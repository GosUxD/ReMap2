package com.example.stefi.remap.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.stefi.remap.R;
import com.example.stefi.remap.view.fragments.MapFragment;
import com.example.stefi.remap.view.fragments.TaskFragment;

/**
 * Created by Stefi on 16.01.2017.
 */

public class MainActivity extends AppCompatActivity {

    ViewPager mViewPager;
    TaskFragment mRemindersFragment;
    TabLayout mTabLayout;
    FloatingActionButton mFab;
    
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mFab = (FloatingActionButton) findViewById(R.id.myFAB);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NewTaskActivity.class));
            }
        });

        mTabLayout.addTab(mTabLayout.newTab().setText("Tasks"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Map"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new TaskFragment();
                    case 1:
                        return new MapFragment();
                    default:
                        return new TaskFragment();
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                 mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
}
