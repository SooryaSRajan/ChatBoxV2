package com.example.chatbox;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.chatbox.user_profile_database.UserProfileTable;
import com.example.chatbox.user_profile_database.profile;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static android.content.ContentValues.TAG;


public class UserListViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private FragmentManager fragmentManager;
    Context context;
    SwipeRefreshLayout pullToRefresh;
    FragmentOne fragmentOne;
    FragmentTwo fragmentTwo;
    FragmentThree fragmentThree;
    ArrayList<HashMap> mMapList = new ArrayList<>();
    TabLayout layout;

    public UserListViewFragment(){
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentTwo = new FragmentTwo();
        fragmentThree = new FragmentThree();
        setHasOptionsMenu(true);

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.search_button).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_user_list_view_fragment, container, false);
        PageAdapter adapter = new PageAdapter(fragmentManager, 0);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        fragmentOne = new FragmentOne(tabLayout);

        adapter.addFragment(fragmentOne, "Friends");
        adapter.addFragment(fragmentTwo, "Requests");
        adapter.addFragment(fragmentThree, "Find Friends");


        final SearchView mSearch = getActivity().findViewById(R.id.search_bar);
        final ViewPager viewPager = view.findViewById(R.id.view_pager);
        pullToRefresh = getActivity().findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(this);

        final Toolbar toolbar = getActivity().findViewById(R.id.search_bar_tool_bar);

        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                if (viewPager.getCurrentItem() == 0)
                    fragmentOne.searchFunction(newText);

                else if (viewPager.getCurrentItem() == 1)
                    fragmentTwo.searchFunction(newText);

                else if (viewPager.getCurrentItem() == 2)
                    fragmentThree.searchFunction(newText);

                if (toolbar.getVisibility() == View.GONE) {

                    Log.e(TAG, "afterTextChanged: Button Gone");
                    if (viewPager.getCurrentItem() == 0)
                        fragmentOne.SearchBackPressed();

                    else if (viewPager.getCurrentItem() == 1)
                        fragmentTwo.SearchBackPressed();

                    else if (viewPager.getCurrentItem() == 2)
                        fragmentThree.SearchBackPressed();

                }
                return false;
            }
        });

        mSearch.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                final View touchView = view.findViewById(R.id.view_pager);
                touchView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });

                Toolbar toolbar = getActivity().findViewById(R.id.search_bar_tool_bar);
                toolbar.setVisibility(View.GONE);

                toolbar = getActivity().findViewById(R.id.tool_bar);
                toolbar.setVisibility(View.VISIBLE);

                layout = getActivity().findViewById(R.id.tab_layout);
                layout.setVisibility(View.VISIBLE);

                return false;
            }
        });


        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);

        /**
         * Tab badge is controlled here
         */
        FirebaseDatabase.getInstance().getReference("UNREAD COUNT").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final ArrayList<String> userList = new ArrayList<>();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("USER ACCOUNT CHANGE");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()) {
                userList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Log.e(TAG, "onDataChange: Observer" + snap.getValue());
                    if (snap.getValue().toString().contains(FirebaseAuth.getInstance().getUid())) {
                        Log.e(TAG, "onDataChange: Observer contains");
                    } else {
                        Log.e(TAG, "onDataChange: Observer !contains");
                        if(!snap.getKey().contains(FirebaseAuth.getInstance().getUid()))
                        userList.add(snap.getKey());
                    }
                }
                if(!userList.isEmpty())
                refreshData(userList);
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    UserListViewFragment(Context context, FragmentManager fragmentManager){
        this.fragmentManager = fragmentManager;
        this.context = context;
    }

    @Override
    public void onRefresh() {
        pullToRefresh.setRefreshing(true);
        refreshData();
        pullToRefresh.setRefreshing(false);
    }

    public void refreshData(final ArrayList<String> refreshList){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("USER ACCOUNT CHANGE");
        final ArrayList<HashMap> mList = new ArrayList<>();
        final DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("USER PROFILE");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < refreshList.size(); i++) {
                    reference.child(refreshList.get(i)).child(FirebaseAuth.getInstance().getUid()).setValue("ADDED");
                    HashMap map = new HashMap();
                    map.put("KEY", refreshList.get(i));
                    map.put("NAME", snapshot.child(refreshList.get(i)).child("NAME").getValue().toString());
                    mList.add(map);
                }
                asyncTask(mList, 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public  void refreshData(){
        mMapList.clear();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("USER PROFILE");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    HashMap map = new HashMap();
                    map.put("KEY",dataSnapshot.getKey());
                    map.put("NAME", Objects.requireNonNull(dataSnapshot.child("NAME").getValue()).toString());
                    mMapList.add(map);
                }
                asyncTask(mMapList, 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void asyncTask(final ArrayList<HashMap> mapList, final int flag) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                        UserProfileTable database = UserProfileTable.getInstance(getActivity());

                        if(flag == 0) {
                            database.dao().deleteAll();
                        }

                        for (HashMap map : mapList) {
                            database = UserProfileTable.getInstance(getActivity());
                            profile object = new profile(Objects.requireNonNull(map.get("KEY")).toString(), map.get("NAME").toString());
                            database.dao().insertProfile(object);
                            Log.e(TAG, "run: " + map.get("NAME").toString());
                        }
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                // UI code goes here
                                fragmentOne.asyncTask();
                                fragmentTwo.asyncTask();
                                fragmentThree.asyncTask();

                            }
                        });


                } catch (Exception e) {
                    Log.e("Table refresher", e.toString());
                }

            }
        };
        thread.start();
    }

}
