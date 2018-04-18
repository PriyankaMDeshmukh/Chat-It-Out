package com.sdsuchatapp.chatitout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by priyankadeshmukh on 4/18/18.
 */

class PageAdapter extends FragmentPagerAdapter {
    private final List<Fragment> listOfFragmentsInTab =new ArrayList<>();
    private final List<String> titleList=new ArrayList<>();

    public void addFragment(Fragment fragment, String title){
        listOfFragmentsInTab.add(fragment);
        titleList.add(title);
    }

      public PageAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public CharSequence getPageTitle(int position){
        return titleList.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return listOfFragmentsInTab.get(position);
    }

    @Override
    public int getCount() {
        return listOfFragmentsInTab.size();
    }
}
