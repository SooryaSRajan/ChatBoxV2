package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chatbox.MessageDatabase.MessageDatabase;
import com.example.chatbox.user_profile_database.UserProfileTable;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.example.chatbox.Constants.PROGRESS_FLAG;
import static com.example.chatbox.Constants.USER_NAME;
import static com.example.chatbox.R.id.user_name_navigation_view;

public class HomePageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG =  "HomePageActicity";
    String ACTION_START_SERVICE = "ACTION_START_SERVICE";
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    TabLayout layout;
    EditText text;
    Fragment fragment;
    ProgressBar progressBar;

    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        progressBar = findViewById(R.id.progress_circular_bar);

        fragment = new UserListViewFragment(this, getSupportFragmentManager());
        mRef.child("ONLINE").child(firebaseUser.getUid()).setValue("ONLINE");
        mRef.child("ONLINE").child(firebaseUser.getUid()).onDisconnect().setValue("OFFLINE");

        updateToken(FirebaseInstanceId.getInstance().getToken());

        toolbar = findViewById(R.id.search_bar_tool_bar);
        toolbar.setVisibility(View.GONE);

        ImageButton searchBack = findViewById(R.id.search_back);

        searchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar = findViewById(R.id.search_bar_tool_bar);
                toolbar.setVisibility(View.GONE);

                toolbar = findViewById(R.id.tool_bar);
                toolbar.setVisibility(View.VISIBLE);

                layout = findViewById(R.id.tab_layout);
                layout.setVisibility(View.VISIBLE);
                text = findViewById(R.id.search_bar);
                text.setText("");


                final View touchView = findViewById(R.id.view_pager);
                touchView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });

            }
        });

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.drawer_layout);

        if(PROGRESS_FLAG) {
            drawerLayout.setVisibility(View.INVISIBLE);
        }
        else{
            RelativeLayout relativeLayout = findViewById(R.id.progress_circular_layout);
            relativeLayout.setVisibility(View.GONE);
        }

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_main, fragment, "USER LIST FRAGMENT");
        fragmentTransaction.commit();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                USER_NAME = snapshot.child("USER PROFILE").child(firebaseUser.getUid()).child("NAME").getValue().toString();
                navigationView = findViewById(R.id.navigation_view);
                View headerView = navigationView.getHeaderView(0);
                TextView textView = headerView.findViewById(user_name_navigation_view);
                textView.setText(USER_NAME);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search_button) {
            layout = findViewById(R.id.tab_layout);
            toolbar = findViewById(R.id.tool_bar);
            toolbar.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
            toolbar = findViewById(R.id.search_bar_tool_bar);
            toolbar.setVisibility(View.VISIBLE);

            final View touchView = findViewById(R.id.view_pager);
            touchView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);

        if (menuItem.getItemId() == R.id.log_out) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are You Sure You Want To Logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    asyncTask();
                    mRef.child("ONLINE").child(firebaseUser.getUid()).setValue("OFFLINE");
                    FirebaseDatabase.getInstance().getReference().child("TOKENS").child(FirebaseAuth.getInstance().getUid()).removeValue();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(HomePageActivity.this, LoginSignUpActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).setNegativeButton("No", null).create();
            builder.show();
        }

        if(menuItem.getItemId() == R.id.user_profile){
            Intent intent = new Intent(HomePageActivity.this, ProfilePictureActivity.class);
            startActivity(intent);
        }

        return true;
    }

    void asyncTask() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                UserProfileTable database = UserProfileTable.getInstance(HomePageActivity.this);
                database.dao().deleteAll();
                Log.e(TAG, "run: Delete table" );
                MessageDatabase database1 = MessageDatabase.getInstance(HomePageActivity.this);
                database1.dao().deleteAll();
            }
        });
    }


    public void updateToken(String mToken){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("TOKENS");
        reference.child(firebaseUser.getUid()).setValue(mToken);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: " );

    }

    @Override
    protected void onPause() {
        super.onPause();
        mRef.child("ONLINE").child(firebaseUser.getUid()).setValue("OFFLINE");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRef.child("ONLINE").child(firebaseUser.getUid()).setValue("ONLINE");

    }
}