package com.mucify;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mucify.ui.internal.ScreenSlidePagerAdapter;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_screen_slide);

        ViewPager2 viewPager = findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);


        try {
            PermissionManager.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            PermissionManager.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            PermissionManager.requestPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
