package com.example.chatbox;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatbox.user_profile_database.UserProfileTable;
import com.example.chatbox.user_profile_database.profile;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static android.content.ContentValues.TAG;


public class user_list_view_fragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private FragmentManager fragmentManager;
    Context context;
    SwipeRefreshLayout pullToRefresh;

    FragmentOne fragmentOne;
    FragmentTwo fragmentTwo;
    FragmentThree fragmentThree;
    ArrayList<HashMap> mMapList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentOne = new FragmentOne();
        fragmentTwo = new FragmentTwo();
        fragmentThree = new FragmentThree();
        setHasOptionsMenu(true);

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.search_button).setVisible(true);
        menu.findItem(R.id.home_button).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_list_view_fragment, container, false);
        PageAdapter adapter = new PageAdapter(fragmentManager, 0);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);


        adapter.addFragment(fragmentOne, "Friends");
        adapter.addFragment(fragmentTwo, "Requests");
        adapter.addFragment(fragmentThree, "Find Friends");


        EditText mSearch = getActivity().findViewById(R.id.search_bar);
        final ViewPager viewPager = view.findViewById(R.id.view_pager);
        pullToRefresh = getActivity().findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(this);


        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(viewPager.getCurrentItem() == 0)
                    fragmentOne.searchFunction(s.toString());

                else if(viewPager.getCurrentItem() == 1)
                    fragmentTwo.searchFunction(s.toString());


                else if(viewPager.getCurrentItem() == 2)
                    fragmentThree.searchFunction(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    user_list_view_fragment(Context context, FragmentManager fragmentManager){
        this.fragmentManager = fragmentManager;
        this.context = context;
    }

    @Override
    public void onRefresh() {
        pullToRefresh.setRefreshing(true);
        refreshData();
        pullToRefresh.setRefreshing(false);
    }

    public  void refreshData(){
       // tableDelete();
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
                asyncTask(mMapList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void asyncTask(final ArrayList<HashMap> mapList) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    UserProfileTable database = UserProfileTable.getInstance(getActivity());
                    database.dao().deleteAll();
                    for(HashMap map : mapList) {
                        database = UserProfileTable.getInstance(getActivity());
                        profile object = new profile(Objects.requireNonNull(map.get("KEY")).toString(), map.get("NAME").toString());
                        database.dao().insertProfile(object);
                        Log.e(TAG, "run: " + map.get("NAME").toString());
                    }
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            // UI code goes here
                            Toast.makeText(getActivity(), "Hello", Toast.LENGTH_SHORT).show();
                            fragmentOne.asyncTask();
                            fragmentOne.ListViewUpdater();
                            fragmentTwo.asyncTask();
                            fragmentTwo.ListViewUpdater();
                            fragmentThree.asyncTask();
                            fragmentThree.ListViewUpdater();

                        }
                    });
                } catch (Exception e) {
                    Log.e("Table refresher", e.toString());
                }

            }
        };
        thread.start();
    }

    private void tableDelete() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserProfileTable database = UserProfileTable.getInstance(getActivity());
                    database.dao().deleteAll();
                } catch (Exception e) {
                    Log.e("Table Deleter", e.toString());
                }

            }
        });
    }

}
