package com.wso2is.androidsample.activities;

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.wso2is.androidsample.R;
import com.wso2is.androidsample.fragments.One;
import com.wso2is.androidsample.navigation.CustomViewPager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TabLayout mTabLayout;
    private Toolbar toolbar;

    private int[] mTabsIcons = {
            R.drawable.home,
            R.drawable.list,
            R.drawable.favorite,
            R.drawable.usercircleicon,
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.items_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                Toast.makeText(this, "Item 1 selected", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_settings:
                Toast.makeText(this, "Item 2 selected", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Setup the viewPager
        CustomViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        if (viewPager != null) {
            viewPager.setPagingEnabled(false);
            pagerAdapter.addFrag(new One(R.layout.activity_login), "Home");
            pagerAdapter.addFrag(new One(R.layout.activity_user), "Categories");
            pagerAdapter.addFrag(new One(R.layout.activity_login), "Favorites");
            pagerAdapter.addFrag(new One(R.layout.activity_login), "User");
            viewPager.setAdapter(pagerAdapter);
        }

        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

    }

    private void setupTabIcons() {
        mTabLayout.getTabAt(0).setIcon(mTabsIcons[0]);
        mTabLayout.getTabAt(1).setIcon(mTabsIcons[1]);
        mTabLayout.getTabAt(2).setIcon(mTabsIcons[2]);
        mTabLayout.getTabAt(3).setIcon(mTabsIcons[3]);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
//            return mFragmentTitleList.get(position);
        }

    }

}
