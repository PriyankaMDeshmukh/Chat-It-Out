package com.sdsuchatapp.chatitout;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.TableLayout;


public class ChatActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private PageAdapter tabbedPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        tabbedPageAdapter=new PageAdapter(getSupportFragmentManager());
        mViewPager=(ViewPager) findViewById(R.id.container);
        setUpViewPager(mViewPager);
        TabLayout tabLayout=(TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


    }
    private void setUpViewPager(ViewPager viewPager){
        PageAdapter tabbedPage=new PageAdapter(getSupportFragmentManager());
        tabbedPage =new PageAdapter(getSupportFragmentManager());
        tabbedPage.addFragment(new AllChatsFragment(),"Chats");
        tabbedPage.addFragment(new AllUsersFragment(),"Friends");
        tabbedPage.addFragment(new ProfileFragment(),"Profile");
        viewPager.setAdapter(tabbedPage);
    }


}
