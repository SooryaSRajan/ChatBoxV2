package com.example.chatbox;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatbox.list_adapters.profileListAdapter;
import com.example.chatbox.user_profile_database.UserProfileTable;
import com.example.chatbox.user_profile_database.profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class FragmentTwo extends Fragment {
    private List<profile> profileList = null;
    private List<HashMap> profileMap = new ArrayList<>(), searchMap = new ArrayList<>(), mainMap = new ArrayList<>();
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    private static ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_two, container, false);

        listView = view.findViewById(R.id.list_view_two);
        asyncTask();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Accept Friend request?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mRef.child("REQUEST").child(Objects.requireNonNull(searchMap.get(position)
                                .get("KEY")).toString()).child(firebaseUser.getUid()).setValue("ACCEPTED");

                        mRef.child("REQUEST").child(firebaseUser.getUid()).child(Objects.requireNonNull(searchMap.get(position)
                                .get("KEY")).toString()).setValue("ACCEPTED");

                        mRef.child("PROFILE ORDER").child(firebaseUser.getUid()).child(Objects.requireNonNull(searchMap.get(position)
                                .get("KEY")).toString()).setValue(getTime());
                        mRef.child("PROFILE ORDER").child(Objects.requireNonNull(searchMap.get(position)
                                .get("KEY")).toString()).child(firebaseUser.getUid()).setValue(getTime());


                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRef.child("REQUEST").child(Objects.requireNonNull(searchMap.get(position)
                                .get("KEY")).toString()).child(firebaseUser.getUid()).removeValue();

                        mRef.child("REQUEST").child(firebaseUser.getUid()).child(Objects.requireNonNull(searchMap.get(position)
                                .get("KEY")).toString()).removeValue();
                    }
                }).create();
                builder.show();


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

                    if(profileMap!=null)
                        profileMap.clear();

                    if(profileList!=null)
                        profileList.clear();

                    if(searchMap!=null)
                        searchMap.clear();

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

                    mRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            profileMap.clear();
                            searchMap.clear();
                            for (int i = 0; i < mainMap.size(); i++) {
                                if (snapshot.child("REQUEST").child(firebaseUser.getUid()).hasChild(Objects.requireNonNull(mainMap.get(i).get("KEY"))
                                        .toString()) ) {
                                    String token =  snapshot.child("REQUEST").child(firebaseUser.getUid()).child(Objects.requireNonNull(mainMap.get(i).get("KEY"))
                                            .toString()).getValue().toString();

                                    if(token.contains("REQUESTED")) {
                                        profileMap.add(mainMap.get(i));
                                        searchMap.add(mainMap.get(i));

                                    }
                                }
                            }

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    ListViewUpdater();
                                }
                            });

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                catch(Exception e) {
                    Log.e("Async List View", e.toString());
                }
            }
        };
        thread.start();
    }

    public void ListViewUpdater(){

        if(getActivity()!=null) {
            profileListAdapter adapter = new profileListAdapter(getActivity(), profileMap);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

    }


    public String getTime(){
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(currentTime);
    }


    public void searchFunction(String string){
        searchMap.clear();
        if (profileMap != null) {
            for (HashMap i : profileMap) {
                if (i.get("NAME").toString().toLowerCase().contains(string.trim().toLowerCase())) {
                    searchMap.add(i);
                    ListView listView = getActivity().findViewById(R.id.list_view_two);
                    profileListAdapter adapter = new profileListAdapter(getActivity(), searchMap);
                    listView.setAdapter(adapter);

                }
            }
            if (searchMap.isEmpty()) {
                Toast.makeText(getContext(), "No Users Found", Toast.LENGTH_SHORT).show();
                ListView listView = getActivity().findViewById(R.id.list_view_two);
                profileListAdapter adapter = new profileListAdapter(getActivity(), searchMap);
                listView.setAdapter(adapter);

            }
        }
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
