package com.mucify;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mucify.ui.OpenPlaylistFragment;
import com.mucify.ui.OpenSongFragment;
import com.mucify.ui.PlaySongFragment;
import com.mucify.ui.internal.ScreenSlidePagerAdapter;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.main_activity);
        setupViewPager();
//        ((ViewPager2)findViewById(R.id.pager)).registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                super.onPageScrollStateChanged(state);
//            }
//        });

        try {
            PermissionManager.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            PermissionManager.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            PermissionManager.requestPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Globals.load(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // onBackButtonClicked in action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(((ViewPager2)findViewById(R.id.pager)).getCurrentItem()) {
            case 0:
                if(getOpenSongFragment() == null) {
                    for(Fragment f : getSupportFragmentManager().getFragments()) {
                        if(f instanceof PlaySongFragment)
                            ((PlaySongFragment)f).unload();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.play_song_fragment, new OpenSongFragment())
                            .addToBackStack(null)
                            .commit();
                }
                break;
            case 1:
                break;
        }
        return true;
    }

    private void setupViewPager() {
        ViewPager2 viewPager = findViewById(R.id.pager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch(position) {
                    case 0:
                        tab.setText(R.string.tab_text_songs);
                        break;
                    case 1:
                        tab.setText(R.string.tab_text_playlists);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid number of tabs (" + position + ")");
                }
            }
        }).attach();
    }

    private OpenSongFragment getOpenSongFragment() {
        ViewPager2 viewPager = findViewById(R.id.pager);
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
        if(viewPager.getCurrentItem() == 0 && page != null)
            return ((OpenSongFragment)page);

        return null;
    }

    private OpenPlaylistFragment getOpenPlaylistFragment() {
        ViewPager2 viewPager = findViewById(R.id.pager);
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
        if(viewPager.getCurrentItem() == 1 && page != null)
            return ((OpenPlaylistFragment)page);

        return null;
    }
}
