package com.example.chatbox;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.chatbox.user_profile_database.UserProfileTable;
import com.example.chatbox.user_profile_database.profile;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static android.content.ContentValues.TAG;


public class user_list_view_fragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private FragmentManager fragmentManager;
    Context context;
    SwipeRefreshLayout pullToRefresh;
    final FragmentOne fragmentOne = new FragmentOne();
    final FragmentTwo fragmentTwo = new FragmentTwo();
    final FragmentThree fragmentThree = new FragmentThree();

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
        tableDelete();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("USER PROFILE");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                   asyncTask(dataSnapshot.getKey(),  Objects.requireNonNull(dataSnapshot.child("NAME").getValue()).toString());
                }
                fragmentOne.asyncTask();
                fragmentOne.ListViewUpdater();
                fragmentTwo.asyncTask();
                fragmentTwo.ListViewUpdater();
                fragmentThree.asyncTask();
                fragmentThree.ListViewUpdater();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void asyncTask(final String userId, final String mName) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserProfileTable database = UserProfileTable.getInstance(getActivity());
                    profile object = new profile(Objects.requireNonNull(userId), mName);
                    database.dao().insertProfile(object);
                    Log.e(TAG, "run: Table refreshed" );

                } catch (Exception e) {
                    Log.e("Table refresher", e.toString());
                }

            }
        });
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
