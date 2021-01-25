package com.wso2is.androidsample.utils;

import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wso2is.androidsample.R;
import com.wso2is.androidsample.fragments.Home;
import com.wso2is.androidsample.navigation.CustomViewPager;
import com.wso2is.androidsample.navigation.ViewPagerAdapter;

public class SetupScreenCustom extends AppCompatActivity {

    private TabLayout mTabLayout;

    private int[] mTabsIcons = {
            R.drawable.home,
            R.drawable.list,
            R.drawable.google_maps,
            R.drawable.favorite,
    };

    public void init(){
        /* ---- START CUSTOM --- */
        // Setup the viewPager
        CustomViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        if (viewPager != null) {
            viewPager.setPagingEnabled(false);

            pagerAdapter.addFrag(new Home(), "Home");
            pagerAdapter.addFrag(new Home(), "Categories");
            pagerAdapter.addFrag(new Home(), "Favorites");
            pagerAdapter.addFrag(new Home(), "User");
            viewPager.setAdapter(pagerAdapter);

            mTabLayout = findViewById(R.id.tab_layout);
            mTabLayout.setupWithViewPager(viewPager);
            setupTabIcons();
        }



        /* --- REMOVE CLICKABLE ON OTHERS TAB BAR --- */
        LinearLayout tabStrip = ((LinearLayout) mTabLayout.getChildAt(0));
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            int finalI = i;
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (finalI > 0)
                        return true;
                    return false;
                }
            });
        }
        /* ---- END CUSTOM --- */
    }


    private void setupTabIcons() {
        mTabLayout.getTabAt(0).setIcon(mTabsIcons[0]);
        mTabLayout.getTabAt(1).setIcon(mTabsIcons[1]);
        mTabLayout.getTabAt(2).setIcon(mTabsIcons[2]);
        mTabLayout.getTabAt(3).setIcon(mTabsIcons[3]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.items_app_bar, menu);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_language:
//                Toast.makeText(this, "Change Language was clicked", Toast.LENGTH_LONG).show();
//                return true;
            case R.id.action_login:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
