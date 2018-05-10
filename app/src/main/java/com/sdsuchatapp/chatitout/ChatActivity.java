package com.sdsuchatapp.chatitout;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;


import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class ChatActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private PageAdapter tabbedPageAdapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        tabbedPageAdapter=new PageAdapter(getSupportFragmentManager());
        mViewPager= findViewById(R.id.container);
        setUpViewPager(mViewPager);
        TabLayout tabLayout= findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




    }
    private void setUpViewPager(ViewPager viewPager){
        PageAdapter tabbedPage=new PageAdapter(getSupportFragmentManager());
        tabbedPage.addFragment(new AllChatsFragment(),"Chats");
        tabbedPage.addFragment(new AllUsersFragment(),"Friends");
        tabbedPage.addFragment(new ProfileFragment(),"Profile");
        viewPager.setAdapter(tabbedPage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                Intent goToStart = new Intent(this, LoginActivity.class);
                startActivity(goToStart);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
