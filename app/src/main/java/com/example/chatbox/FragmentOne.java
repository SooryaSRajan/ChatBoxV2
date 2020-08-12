package com.example.chatbox;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FragmentOne extends Fragment {
    private List<profile> profileList = null;
    private List<String> profileString = new ArrayList<>(), search = new ArrayList<>();
    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_one, container, false);
        asyncTask(view);

      //  profileListAdapter adapter = new profileListAdapter(getActivity(), profileString);
        //ListView listView = view.findViewById(R.id.list_view);
        //listView.setAdapter(adapter);

        return view;
    }


    void asyncTask(final View view) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    UserProfileTable database = UserProfileTable.getInstance(getContext());

                    //database.dao().deleteAll();

                    if(profileString!=null)
                        profileString.clear();

                    if(profileList!=null)
                        profileList.clear();


                    if(search!=null)
                        search.clear();

                    profileList = database.dao().getProfile();
                    Log.e(TAG, "run: For Loop out");

                    for(int i = 0; i<profileList.size(); i++){
                        profile mProfile = profileList.get(i);
                        if(!mProfile.user_key.contains(firebaseUser.getUid())) {
                            profileString.add(mProfile.name);
                            search.add(mProfile.name);
                            Log.e(TAG, "run: For Loop");
                        }
                    }

                    //ListView listView = view.findViewById(R.id.list_view);
                   // ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_content, R.id.text_set_list, search);
                    //listView.setAdapter(adapter);
                }
            catch(Exception e){
                Log.e("Async List View", e.toString());
            }

        }});
}

public void searchFunction(String string){
    Log.e(TAG, "searchFunction: 1" + string );
    search.clear();
    if (profileString != null) {
        for (String i : profileString) {
            if (i.toLowerCase().contains(string.trim().toLowerCase())) {
              //  search.add(i);
                //ListView listView = getActivity().findViewById(R.id.list_view);

                //ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_content, R.id.text_set_list, search);
               // listView.setAdapter(adapter);
            }
        }
        if (search.isEmpty()) {
            //ListView listView = getActivity().findViewById(R.id.list_view);
            //search.add("No Users Found");
           // ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_content, R.id.text_set_list, search);
          //  listView.setAdapter(adapter);
        }
    }
}
}
