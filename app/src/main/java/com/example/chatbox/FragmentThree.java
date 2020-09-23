package com.example.chatbox;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.os.AsyncTask;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FragmentThree extends Fragment {
    private List<profile> profileList = null;
    private List<HashMap> profileMap = new ArrayList<>(), searchMap = new ArrayList<>();
    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("REQUEST");
    DatabaseReference mDateRef = FirebaseDatabase.getInstance().getReference().child("PROFILE ORDER");
    private static ListView listView;
    private TextView noUserFound;
    private View view;
    private View requestBuilderView;
    private Button mCancel;
    private TextView mUnfollowBuilderTitle, mUnfollowBuilderSubTitle;
    private Button mFriend, mFriendCancel;
    private TextView mfollowBuilderTitle, mFollowerBuilderSubTitle;
    private int currentListPosition = 0;
    ProfileListAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_three, container, false);
        noUserFound = view.findViewById(R.id.no_user_found_3);
        noUserFound.setVisibility(View.GONE);

        View builderView = getLayoutInflater().inflate(R.layout.unfollow_alert_layout, null);
        requestBuilderView = getLayoutInflater().inflate(R.layout.request_alert_layout, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(builderView);
        final AlertDialog alertDialog = builder.create();

        final AlertDialog.Builder friendBuilder = new AlertDialog.Builder(getActivity());
        friendBuilder.setView(requestBuilderView);
        final AlertDialog alertDialogFriend = friendBuilder.create();

        Button mUnfollow = builderView.findViewById(R.id.unfollow_confirm);
        mCancel = builderView.findViewById(R.id.unfollow_cancel);
        mUnfollowBuilderSubTitle = builderView.findViewById(R.id.unfollow_sub_title);
        mUnfollowBuilderTitle = builderView.findViewById(R.id.unfollow_title);


        mFriend = requestBuilderView.findViewById(R.id.friend_confirm);
        mFriendCancel = requestBuilderView.findViewById(R.id.friend_cancel);
        mfollowBuilderTitle = requestBuilderView.findViewById(R.id.friend_title);
        mFollowerBuilderSubTitle = requestBuilderView.findViewById(R.id.friend_subtitle);

        mFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child(Objects.requireNonNull(searchMap.get(currentListPosition)
                        .get("KEY")).toString()).child(firebaseUser.getUid()).setValue("REQUESTED");
                alertDialogFriend.dismiss();

            }
        });

        mFriendCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogFriend.dismiss();
            }
        });


        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child(Objects.requireNonNull(searchMap.get(currentListPosition)
                        .get("KEY")).toString()).child(firebaseUser.getUid()).removeValue();

                mRef.child(firebaseUser.getUid()).child(Objects.requireNonNull(searchMap.get(currentListPosition)
                        .get("KEY")).toString()).removeValue();

                mDateRef.child(Objects.requireNonNull(searchMap.get(currentListPosition)
                        .get("KEY")).toString()).child(firebaseUser.getUid()).removeValue();

                mDateRef.child(firebaseUser.getUid()).child(Objects.requireNonNull(searchMap.get(currentListPosition)
                        .get("KEY")).toString()).removeValue();
                alertDialog.dismiss();

            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        listView = view.findViewById(R.id.list_view_three);
        adapter = new ProfileListAdapter(getActivity(), searchMap);
        listView.setAdapter(adapter);

        asyncTask();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                currentListPosition = position;
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(Objects.requireNonNull(searchMap.get(position)
                                .get("KEY")).toString()).hasChild(firebaseUser.getUid())){

                            String token = snapshot.child(Objects.requireNonNull(searchMap.get(position)
                                    .get("KEY")).toString()).child(firebaseUser.getUid()).getValue().toString();

                            if(token.contains("ACCEPTED")){

                                mUnfollowBuilderTitle.setText("Unfriend, " + searchMap.get(position).get("NAME").toString() + "?");
                                mUnfollowBuilderSubTitle.setText("Are you sure you want to unfriend " +
                                        searchMap.get(position).get("NAME").toString() + "?" +"\nYou " +
                                        "won't be able to view chats or text unless you're friends");
                                alertDialog.show();

                            }
                            else
                            Toast.makeText(getActivity(), "Request already sent", Toast.LENGTH_SHORT).show();
                        }

                        else{
                            mfollowBuilderTitle.setText("Request " + searchMap.get(position).get("NAME").toString() + "?");
                            mFollowerBuilderSubTitle.setText("Are you sure you want to send request to " + searchMap.get(position).get("NAME").toString() + "? \nYou won't be able to cancel your request once its sent");
                            alertDialogFriend.show();


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
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    // UI code goes here
                                    ListViewUpdater();

                                }
                            });

                            Log.e(TAG, "run: For Loop");
                        }
                    }

                }
                catch(Exception e){
                    Log.e("Async List View", e.toString());
                }

            }});
    }

    public void ListViewUpdater(){

        if(getActivity()!=null) {
            adapter.notifyDataSetChanged();
        }

    }

    public void searchFunction(String string) {
        Log.e(TAG, "searchFunction: 1" + string);
        Log.e(TAG, "searchFunction: 1" + string);
        searchMap.clear();
        noUserFound = view.findViewById(R.id.no_user_found_3);
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
