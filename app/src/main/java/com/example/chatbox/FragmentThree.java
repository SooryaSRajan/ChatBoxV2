package com.example.chatbox;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FragmentThree extends Fragment {
    private List<profile> profileList = null;
    private List<HashMap> profileMap = new ArrayList<>(), searchMap = new ArrayList<>();
    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("REQUEST");
    private static ListView listView;
    SwipeRefreshLayout pullToRefresh;
    ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_three, container, false);
        listView = view.findViewById(R.id.list_view_three);
        asyncTask();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(Objects.requireNonNull(profileMap.get(position)
                                .get("KEY")).toString()).hasChild(firebaseUser.getUid())){

                            String token = snapshot.child(Objects.requireNonNull(profileMap.get(position)
                                    .get("KEY")).toString()).child(firebaseUser.getUid()).getValue().toString();

                            if(token.contains("ACCEPTED")){

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Unfriend " +
                                        profileMap.get(position).get("NAME").toString() + " ?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mRef.child(Objects.requireNonNull(profileMap.get(position)
                                                    .get("KEY")).toString()).child(firebaseUser.getUid()).removeValue();

                                                mRef.child(firebaseUser.getUid()).child(Objects.requireNonNull(profileMap.get(position)
                                                        .get("KEY")).toString()).removeValue();

                                            }
                                        }).setNegativeButton("No", null).create();
                                builder.show();

                                Toast.makeText(getActivity(), "Already Friends", Toast.LENGTH_SHORT).show();
                            }
                            else
                            Toast.makeText(getActivity(), "Request already sent", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Do you want to send request to " +
                                    profileMap.get(position).get("NAME").toString())
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mRef.child(Objects.requireNonNull(profileMap.get(position)
                                                    .get("KEY")).toString()).child(firebaseUser.getUid()).setValue("REQUESTED");

                                        }
                                    }).setNegativeButton("No", null).create();
                            builder.show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        return view;
    }


    void asyncTask() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserProfileTable database = UserProfileTable.getInstance(getContext());

                    if(profileMap!=null)
                        profileMap.clear();

                    if(profileList!=null)
                        profileList.clear();


                    if(searchMap!=null)
                        searchMap.clear();

                    profileList = database.dao().getProfile();
                    Log.e(TAG, "run: For Loop out");

                    for(int i = 0; i<profileList.size(); i++){
                        profile mProfile = profileList.get(i);
                        if(!mProfile.user_key.contains(firebaseUser.getUid())) {
                            HashMap map = new HashMap();
                            map.put("NAME", mProfile.name);
                            map.put("KEY", mProfile.user_key);
                            profileMap.add(map);
                            searchMap.add(map);
                            Log.e(TAG, "run: For Loop");
                        }
                    }
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            // UI code goes here
                            ListViewUpdater();

                        }
                    });

                }
                catch(Exception e){
                    Log.e("Async List View", e.toString());
                }

            }});
    }

    public void ListViewUpdater(){

        if(getActivity()!=null) {
            profileListAdapter adapter = new profileListAdapter(getActivity(), profileMap);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

    }

    public void searchFunction(String string){
        Log.e(TAG, "searchFunction: 1" + string );
/*        search.clear();
        if (profileString != null) {
            for (String i : profileString) {
                if (i.toLowerCase().contains(string.trim().toLowerCase())) {
                    search.add(i);
                    ListView listView = getActivity().findViewById(R.id.list_view);
                    profileListAdapter adapter = new profileListAdapter(getActivity(), search);
                     listView.setAdapter(adapter);
                }
            }
            if (search.isEmpty()) {
                ListView listView = getActivity().findViewById(R.id.list_view);
                search.add("No Users Found");
                profileListAdapter adapter = new profileListAdapter(getActivity(), search);
                  listView.setAdapter(adapter);
            }
        }*/
    }

    @Override
    public void onDestroyView() {
        Log.e(TAG, "onDestroyView: View Destroyed 3" );
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "onDetach: Frag Detached 3");
        super.onDetach();
    }
    @Override
    public void onResume() {
        Log.e(ContentValues.TAG, "onResume: Fragment 3" );
        super.onResume();

        if(!profileMap.isEmpty()){
            ListViewUpdater();
        }
    }
}
