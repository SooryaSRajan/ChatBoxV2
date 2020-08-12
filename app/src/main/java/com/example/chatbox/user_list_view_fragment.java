package com.example.chatbox;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;


public class user_list_view_fragment extends Fragment {
    private FragmentManager fragmentManager;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_list_view_fragment, container, false);
        PageAdapter adapter = new PageAdapter(fragmentManager, 0);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        final FragmentOne fragmentOne = new FragmentOne();
        final FragmentTwo fragmentTwo = new FragmentTwo();
        final FragmentThree fragmentThree = new FragmentThree();

        adapter.addFragment(fragmentOne, "Friends");
        adapter.addFragment(fragmentTwo, "Requests");
        adapter.addFragment(fragmentThree, "Find Friends");

        EditText mSearch = getActivity().findViewById(R.id.search_bar);
        final ViewPager viewPager = view.findViewById(R.id.view_pager);

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
}
