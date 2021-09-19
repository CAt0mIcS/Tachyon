package com.mucify.ui.internal;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mucify.ui.OpenPlaylistFragment;
import com.mucify.ui.OpenSongFragment;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {
    public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                return new OpenSongFragment();
            case 1:
                return new OpenPlaylistFragment();
        }
        throw new IllegalArgumentException("Position " + position + " is invalid (0-1)");
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
