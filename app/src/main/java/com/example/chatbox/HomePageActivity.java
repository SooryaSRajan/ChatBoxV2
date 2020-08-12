package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.chatbox.user_profile_database.UserProfileTable;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class HomePageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    TabLayout layout;
    EditText text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

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
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_main, new user_list_view_fragment(this, getSupportFragmentManager()));
        fragmentTransaction.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
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
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        if (menuItem.getItemId() == R.id.log_out) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are You Sure You Want To Logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseAuth.getInstance().signOut();
                    asyncTask();
                    Intent intent = new Intent(HomePageActivity.this, LoginSignUpActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).setNegativeButton("No", null).create();
            builder.show();
        }

        return true;
    }

    void asyncTask() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                UserProfileTable database = UserProfileTable.getInstance(HomePageActivity.this);
                database.dao().deleteAll();
            }
        });
    }
}