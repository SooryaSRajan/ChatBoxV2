package com.example.chatbox;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatbox.FCMNotifications.APIInterface;
import com.example.chatbox.FCMNotifications.NotificationBody;
import com.example.chatbox.FCMNotifications.NotificationContent;
import com.example.chatbox.FCMNotifications.RetrofitClient;
import com.example.chatbox.list_adapters.ProfileListAdapter;
import com.example.chatbox.user_profile_database.UserProfileTable;
import com.example.chatbox.user_profile_database.profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.chatbox.Constants.USER_NAME;


public class FragmentTwo extends Fragment {
    private List<profile> profileList = null;
    private List<HashMap> profileMap = new ArrayList<>(), searchMap = new ArrayList<>(), mainMap = new ArrayList<>();
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    private ListView listView;
    private ValueEventListener listener;
    private Boolean listenerFlag = true;
    private TextView noUserFound;
    private View view;
    TextView title, subtitle;
    Button accept, deny;
    int listPosition = 0;
    ProfileListAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_two, container, false);
        noUserFound = view.findViewById(R.id.no_user_found_2);
        noUserFound.setVisibility(View.GONE);

        final View builderView = getLayoutInflater().inflate(R.layout.accept_request, null);

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setView(builderView);
        final android.app.AlertDialog alertDialog = builder.create();

        title = builderView.findViewById(R.id.accept_request_title);
        subtitle = builderView.findViewById(R.id.accept_request_sub_title);
        accept = builderView.findViewById(R.id.accept_request_confirm);
        deny = builderView.findViewById(R.id.accept_request_deny);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child("REQUEST").child(Objects.requireNonNull(searchMap.get(listPosition)
                        .get("KEY")).toString()).child(firebaseUser.getUid()).setValue("ACCEPTED");

                mRef.child("REQUEST").child(firebaseUser.getUid()).child(Objects.requireNonNull(searchMap.get(listPosition)
                        .get("KEY")).toString()).setValue("ACCEPTED");

                mRef.child("PROFILE ORDER").child(firebaseUser.getUid()).child(Objects.requireNonNull(searchMap.get(listPosition)
                        .get("KEY")).toString()).setValue(getTime());
                mRef.child("PROFILE ORDER").child(Objects.requireNonNull(searchMap.get(listPosition)
                        .get("KEY")).toString()).child(firebaseUser.getUid()).setValue(getTime());
                alertDialog.dismiss();


            }
        });

        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child("REQUEST").child(Objects.requireNonNull(searchMap.get(listPosition)
                        .get("KEY")).toString()).child(firebaseUser.getUid()).removeValue();

                mRef.child("REQUEST").child(firebaseUser.getUid()).child(Objects.requireNonNull(searchMap.get(listPosition)
                        .get("KEY")).toString()).removeValue();

                alertDialog.dismiss();
            }
        });

        listView = view.findViewById(R.id.list_view_two);
        adapter = new ProfileListAdapter(getActivity(), searchMap);
        listView.setAdapter(adapter);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                profileMap.clear();
                searchMap.clear();
                adapter.notifyDataSetChanged();
                if(listenerFlag)
                    for (int i = 0; i < mainMap.size(); i++) {
                        if (snapshot.child("REQUEST").child(firebaseUser.getUid()).hasChild(Objects.requireNonNull(mainMap.get(i).get("KEY"))
                                .toString()) ) {
                            String token =  snapshot.child("REQUEST").child(firebaseUser.getUid()).child(Objects.requireNonNull(mainMap.get(i).get("KEY"))
                                    .toString()).getValue().toString();

                            if(token.contains("REQUESTED")) {
                                profileMap.add(mainMap.get(i));
                                searchMap.add(mainMap.get(i));
                                adapter.notifyDataSetChanged();

                            }
                        }
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        asyncTask();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                listPosition = position;
                title.setText("Accept " + searchMap.get(position).get("NAME").toString() + " request?");
                subtitle.setText("Are you sure you want to accept " +
                        searchMap.get(position).get("NAME").toString() + " Request?" +"\nYou " +
                        "will be able to view and send chats as long as you're friends");
                alertDialog.show();

            }
        });

        return view;
    }


    void asyncTask() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    UserProfileTable database = UserProfileTable.getInstance(getContext());

                    if(profileList!=null)
                        profileList.clear();

                    if(mainMap!=null)
                        mainMap.clear();

                    profileList = database.dao().getProfile();
                    Log.e(TAG, "run: For Loop out");

                    for(int i = 0; i<profileList.size(); i++){
                        profile mProfile = profileList.get(i);
                        if(!mProfile.user_key.contains(firebaseUser.getUid())) {
                            HashMap map = new HashMap();
                            map.put("NAME", mProfile.name);
                            map.put("KEY", mProfile.user_key);
                            mainMap.add(map);
                            Log.e(TAG, "run: For Loop");
                        }
                    }
                    mRef.addValueEventListener(listener);
                }
                catch(Exception e) {
                    Log.e("Async List View 2", e.toString());
                }

            }
        };
        thread.start();
    }

    public void ListViewUpdater(){

        if(getActivity()!=null && adapter!=null) {
            adapter.notifyDataSetChanged();
        }

    }


    public String getTime(){
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(currentTime);
    }


    public void searchFunction(String string){
        searchMap.clear();
        noUserFound = view.findViewById(R.id.no_user_found_2);
        listenerFlag = false;
        if (profileMap != null) {
            for (HashMap i : profileMap) {
                if (i.get("NAME").toString().toLowerCase().contains(string.trim().toLowerCase())) {
                    searchMap.add(i);
                    noUserFound.setVisibility(View.GONE);
                    ListViewUpdater();

                }
            }
            if (searchMap.isEmpty()) {
                noUserFound.setVisibility(View.VISIBLE);
                ListViewUpdater();
            }
        }
    }
    public void SearchBackPressed(){
        noUserFound.setVisibility(View.GONE);
        mRef.addValueEventListener(listener);
        listenerFlag = true;

    }
    @Override
    public void onDestroyView() {
        Log.e(TAG, "onDestroyView: View Destroyed 2" );
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "onDetach: Frag Detached 2");
        super.onDetach();
    }


    }
