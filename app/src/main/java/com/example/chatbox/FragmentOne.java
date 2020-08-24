package com.example.chatbox;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class FragmentOne extends Fragment {
    private List<profile> profileList = null;
    private List<HashMap> profileMap = new ArrayList<>(), searchMap = new ArrayList<>(), mainMap = new ArrayList<>();
    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    private static ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_one, container, false);
        listView = view.findViewById(R.id.list_view);
        asyncTask();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent = new Intent(FragmentOne.super.getContext(), chatListActivity.class);
                intent.putExtra("KEY", profileMap.get(position).get("KEY").toString());
                intent.putExtra("NAME", profileMap.get(position).get("NAME").toString());
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
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

                    if(mainMap!=null)
                        mainMap.clear();

                    profileList = database.dao().getProfile();
                    Log.e(TAG, "FRagment One Run: For Loop out");

                    for(int i = 0; i<profileList.size(); i++){
                        profile mProfile = profileList.get(i);
                        if(!mProfile.user_key.contains(firebaseUser.getUid())) {
                            HashMap map = new HashMap();
                            map.put("NAME", mProfile.name);
                            map.put("KEY", mProfile.user_key);
                            mainMap.add(map);
                            Log.e(TAG, "Fragment One Run: For Loop in");
                        }
                    }


                    ValueEventListener listener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            profileMap.clear();
                            searchMap.clear();
                            for (int i = 0; i < mainMap.size(); i++) {
                                try {
                                    Log.e(TAG, "Frag One Data Change" );
                                    if (snapshot.child("REQUEST").child(firebaseUser.getUid()).hasChild(Objects.requireNonNull(mainMap.get(i).get("KEY"))
                                            .toString())) {
                                        String token = snapshot.child("REQUEST").child(firebaseUser.getUid()).child(Objects.requireNonNull(mainMap.get(i).get("KEY"))
                                                .toString()).getValue().toString();

                                        if (token.contains("ACCEPTED")) {
                                            profileMap.add(mainMap.get(i));
                                            searchMap.add(mainMap.get(i));
                                        }
                                    }
                                }
                                catch (Exception e){

                                }
                            }
                            ListViewUpdater();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    mRef.addValueEventListener(listener);

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

}
