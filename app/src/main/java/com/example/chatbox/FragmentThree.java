package com.example.chatbox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_one, container, false);
        asyncTask(view);

        profileListAdapter adapter = new profileListAdapter(getActivity(), profileMap);
        ListView listView = view.findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Toast.makeText(getActivity(), profileMap.get(position).get("NAME").toString(), Toast.LENGTH_SHORT).show();

                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("REQUEST").child(Objects.requireNonNull(profileMap.get(position)
                                .get("KEY")).toString()).hasChild(firebaseUser.getUid())){
                            Toast.makeText(getActivity(), "Request already sent", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Do you want to send request to " +
                                    profileMap.get(position).get("NAME").toString())
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                  //          mRef.child("REQUEST").child(Objects.requireNonNull(profileMap.get(position)
                                    //                .get("KEY")).toString()).child(firebaseUser.getUid()).setValue("REQUESTED");
                                            mRef.child("REQUEST").child(firebaseUser.getUid()).child(Objects.requireNonNull(profileMap.get(position)
                                                    .get("KEY")).toString()).setValue("REQUESTED");

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


    void asyncTask(final View view) {
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
                }
                catch(Exception e){
                    Log.e("Async List View", e.toString());
                }

            }});
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

}
