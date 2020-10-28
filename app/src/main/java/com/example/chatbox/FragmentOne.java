package com.example.chatbox;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.chatbox.Constants.PROGRESS_FLAG;

public class FragmentOne extends Fragment {
    FrameLayout relativeLayout;
    ValueEventListener onlineListener;
    ValueEventListener mainListener;
    TextView noUserFound;
    private List<profile> profileList = null;
    private List<HashMap> profileMap = new ArrayList<>(), searchMap = new ArrayList<>(), mainMap = new ArrayList<>();
    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("REQUEST");
    DatabaseReference unreadCount = FirebaseDatabase.getInstance().getReference().child("UNREAD COUNT");
    DatabaseReference onlineCount = FirebaseDatabase.getInstance().getReference().child("ONLINE");
    View view;
    DatabaseReference mOrderRef = FirebaseDatabase.getInstance().getReference().child("PROFILE ORDER").child(firebaseUser.getUid());
    private ValueEventListener listener;
    Boolean listenerFlag = true;
    ProfileListAdapter adapter;
    private Boolean flag = false;

    private ValueEventListener countListener;
    private static ListView listView;

    public FragmentOne(){
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e(ContentValues.TAG, "onCreate: Fragment 1" );

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e(TAG, "onCreateView: Frag 1" );
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_one, container, false);

        listView = view.findViewById(R.id.list_view);
        Log.e(TAG, "onCreateView: l1" + R.id.list_view);
        Log.e(TAG, "onCreateView: l2" + R.id.list_view_two);
        Log.e(TAG, "onCreateView: l3" + R.id.list_view_three);
        adapter = new ProfileListAdapter(getActivity(), searchMap);
        listView.setAdapter(adapter);

        noUserFound = view.findViewById(R.id.no_user_found_1);
        noUserFound.setVisibility(View.GONE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent = new Intent(FragmentOne.super.getContext(), ChatListActivity.class);
                intent.putExtra("KEY", searchMap.get(position).get("KEY").toString());
                intent.putExtra("NAME", searchMap.get(position).get("NAME").toString());

                startActivity(intent);
                getActivity().finish();
            }
        });

        mainListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                profileMap.clear();
                searchMap.clear();
                if(listenerFlag) {
                    for (int i = 0; i < mainMap.size(); i++) {
                        try {
                            Log.e(TAG, "Frag One Data Change");
                            if (snapshot.child(firebaseUser.getUid()).hasChild(Objects.requireNonNull(mainMap.get(i).get("KEY"))
                                    .toString())) {
                                String token = snapshot.child(firebaseUser.getUid()).child(Objects.requireNonNull(mainMap.get(i).get("KEY"))
                                        .toString()).getValue().toString();

                                if (token.contains("ACCEPTED")) {
                                    profileMap.add(mainMap.get(i));
                                    searchMap.add(mainMap.get(i));
                                    ListViewUpdater();
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                    UnreadCount();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        asyncTask();
        return view;
    }


    void asyncTask() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserProfileTable database = UserProfileTable.getInstance(getContext());

                    if(profileList!=null)
                        profileList.clear();

                    if(mainMap!=null)
                        mainMap.clear();

                    profileList = database.dao().getProfile();
                    Log.e(TAG, "Fragment One Run: For Loop out");

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

                    mRef.addValueEventListener(mainListener);
                }
                catch(Exception e){
                    Log.e("Async List View 1", e.toString());
                }

            }});
    }

    void UnreadCount(){
        countListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(listenerFlag)
                for(int i = 0; i< profileMap.size(); i++) {
                    try {
                        HashMap map = profileMap.get(i);
                        String count = snapshot.child(map.get("KEY").toString()).child(firebaseUser.getUid()).getValue().toString();
                        if(!count.contains("0"))
                            map.put("COUNT", count);
                        profileMap.set(i, map);
                        searchMap.set(i, map);
                        ListViewUpdater();
                    }
                    catch (Exception e){

                    }
                }
                ListViewSorter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        unreadCount.addValueEventListener(countListener);

         onlineListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(listenerFlag)
                for (int i = 0; i < profileMap.size(); i++) {
                    try {
                        HashMap mMap = profileMap.get(i);
                        String mStatus = snapshot.child(mMap.get("KEY").toString()).getValue().toString();
                        mMap.put("ONLINE", mStatus);
                        profileMap.set(i, mMap);
                        searchMap.set(i, mMap);
                        Log.e(TAG, "onDataOnline: Called") ;
                        ListViewUpdater();

                    } catch (Exception ignored) {
                        Log.e(TAG, "onDataOnline: " + ignored.toString());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        onlineCount.addValueEventListener(onlineListener);

        FirebaseDatabase.getInstance().getReference().child("LAST MESSAGE").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(listenerFlag) {
                    for (int i = 0; i < profileMap.size(); i++) {
                        try {
                            HashMap mMap = profileMap.get(i);
                            String lastMessage = snapshot.child(firebaseUser.getUid()).child(mMap.get("KEY").toString()).getValue().toString();
                            mMap.put("LAST MESSAGE", lastMessage);
                            Log.e(TAG, "onDataChange: " + lastMessage);
                            profileMap.set(i, mMap);
                            searchMap.set(i, mMap);
                            ListViewUpdater();
                        } catch (Exception e) {

                        }
                    }
                    if(PROGRESS_FLAG) {
                        RelativeLayout relativeLayout = getActivity().findViewById(R.id.progress_circular_layout);
                        relativeLayout.setVisibility(View.GONE);

                        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
                        drawerLayout.setVisibility(View.VISIBLE);
                        PROGRESS_FLAG = false;
                    }

                }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void ListViewUpdater() {

        if (getActivity() != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void searchFunction(String string){
        Log.e(TAG, "searchFunction: 1" + string ); searchMap.clear();

        listenerFlag = false;
        noUserFound = view.findViewById(R.id.no_user_found_1);
        if (profileMap != null) {
            for (HashMap i : profileMap) {
                if (i.get("NAME").toString().toLowerCase().contains(string.trim().toLowerCase())) {
                    noUserFound.setVisibility(View.GONE);
                    searchMap.add(i);
                    adapter.notifyDataSetChanged();
                }
            }
            if (searchMap.isEmpty()) {
                noUserFound.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void ListViewSorter(){
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(listenerFlag) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Log.e(TAG, "onDataChange: sorter" + snap.getKey());
                        for (int j = 0; j <= profileMap.size() - 1; j++) {
                            HashMap map = profileMap.get(j);
                            if (snap.getKey().contains(map.get("KEY").toString())) {
                                Log.e(TAG, "onDataChange Sorter: Data Add Error");
                                map.put("DATE", snap.getValue().toString());
                                try {
                                    searchMap.set(j, map);
                                    profileMap.set(j, map);
                                    ListViewUpdater();
                                } catch (Exception e) {

                                }

                            }
                        }

                    }

                    Collections.sort(profileMap, new Comparator<HashMap>() {
                        @Override
                        public int compare(HashMap o1, HashMap o2) {
                            return o1.get("DATE").toString().compareTo(o2.get("DATE").toString());
                        }
                    });


                    Collections.sort(searchMap, new Comparator<HashMap>() {
                        @Override
                        public int compare(HashMap o1, HashMap o2) {
                            return o1.get("DATE").toString().compareTo(o2.get("DATE").toString());
                        }
                    });

                    Collections.reverse(profileMap);
                    Collections.reverse(searchMap);
                    ListViewUpdater();
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mOrderRef.addValueEventListener(listener);
    }

    public void SearchBackPressed(){
        noUserFound.setVisibility(View.GONE);
       // stViewUpdater();
        listenerFlag = true;

        mOrderRef.addValueEventListener(listener);
        onlineCount.addValueEventListener(onlineListener);
        unreadCount.addValueEventListener(countListener);
        mRef.addValueEventListener(mainListener);

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
