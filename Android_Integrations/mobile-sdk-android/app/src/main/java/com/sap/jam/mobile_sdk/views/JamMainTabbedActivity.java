package com.sap.jam.mobile_sdk.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.sap.jam.mobile_sdk.JamSDKDemoApp;
import com.sap.jam.mobile_sdk.R;

public class JamMainTabbedActivity extends AppCompatActivity {

    ViewPager viewPager;
    FragmentPagerAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jam_main);

        pageAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Class clss;
                switch(position) {
                    case 0:
                        clss = JamFeedWidgetFragment.class;
                        break;
                    case 1:
                        clss = JamGroupsListRename.class;
                        break;
                    default:
                        throw new RuntimeException("Invalid tab index: " + position);
                }
                return Fragment.instantiate(JamSDKDemoApp.getAppContext(), clss.getName());
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(pageAdapter);

    }
}
