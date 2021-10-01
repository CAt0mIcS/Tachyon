package com.mucify;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mucify.ui.CreatePlaylistFragment;
import com.mucify.ui.EditPlaylistFragment;
import com.mucify.ui.OpenPlaylistFragment;
import com.mucify.ui.OpenSongFragment;
import com.mucify.ui.PlayPlaylistFragment;
import com.mucify.ui.PlaySongFragment;
import com.mucify.ui.internal.ScreenSlidePagerAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
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

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_layout, menu);
        return true;
    }

    // onBackButtonClicked in action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.mm_btnBack) {
            return onHomeButtonClicked();
        }
        else if(id == R.id.mm_btnImport) {
            return onImportClicked();
        }
        else if(id == R.id.mm_btnExport) {
            return onExportClicked();
        }
        return super.onOptionsItemSelected(item);
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

    private boolean onHomeButtonClicked() {
        PlaySongFragment playSongFragment = null;
        PlayPlaylistFragment playPlaylistFragment = null;
        CreatePlaylistFragment createPlaylistFragment = null;
        EditPlaylistFragment editPlaylistFragment = null;
        for(Fragment f : getSupportFragmentManager().getFragments()) {
            if(f instanceof PlaySongFragment)
                playSongFragment = (PlaySongFragment)f;
            else if(f instanceof PlayPlaylistFragment)
                playPlaylistFragment = (PlayPlaylistFragment)f;
            else if(f instanceof  CreatePlaylistFragment)
                createPlaylistFragment = (CreatePlaylistFragment)f;
            else if(f instanceof EditPlaylistFragment)
                editPlaylistFragment = (EditPlaylistFragment)f;
        }

        switch(((ViewPager2)findViewById(R.id.pager)).getCurrentItem()) {
            case 0:
                if(getOpenSongFragment() == null) {
                    // Removing and adding it works better. Replacing it causes open_song_fragment to have two scrollbars?
                    // And the ListView to not be scrollable anymore?
                    if(playSongFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .remove(playSongFragment)
                                .add(R.id.open_song_fragment, new OpenSongFragment())
                                .addToBackStack(null)
                                .commit();

                        playSongFragment.unload();
                    }
                }
                break;
            case 1:
                if(getOpenPlaylistFragment() == null) {
                    Fragment fragmentToUnload = null;
                    if(playPlaylistFragment != null)
                        fragmentToUnload = playPlaylistFragment;
                    else if(createPlaylistFragment != null)
                        fragmentToUnload = createPlaylistFragment;
                    else if(editPlaylistFragment != null)
                        fragmentToUnload = editPlaylistFragment;


                    if(fragmentToUnload != null) {
                        getSupportFragmentManager().beginTransaction()
                                .remove(fragmentToUnload)
                                .add(R.id.open_playlist_fragment, new OpenPlaylistFragment())
                                .addToBackStack(null)
                                .commit();

                        if(fragmentToUnload == playPlaylistFragment)
                            playPlaylistFragment.unload();
                    }
                }
                break;
        }
        return true;
    }

    private boolean onImportClicked() {
        for(File file : Globals.DataDirectory.listFiles()) {
            file.delete();
        }

        try {
            File saveDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/com.mucify/");
            if(!saveDir.exists()) {
                Utils.messageBox(this, "Error", "Import directory " + saveDir + " doesn't exist.");
                return true;
            }

            for(File file : saveDir.listFiles()) {
                copyFile(file, new File(Globals.DataDirectory.getPath() + "/" + file.getName()));
            }

            Globals.loadAvailableLoops();
            Globals.loadAvailablePlaylists();
            for(Fragment f : getSupportFragmentManager().getFragments()) {
                if(f instanceof OpenSongFragment)
                    ((OpenSongFragment)f).updateLists();
                else if(f instanceof OpenPlaylistFragment)
                    ((OpenPlaylistFragment)f).updateLists();
            }
        } catch(IOException e) {
                Utils.messageBox(this, "Failed to import data", e.getMessage());
            }

        return true;
    }

    private boolean onExportClicked() {
        String destDirPath = Globals.MusicDirectory + "/com.mucify/";
        File destDir = new File(destDirPath);
        if(destDir.exists())
            destDir.delete();
        destDir.mkdirs();

        try {
            for(File file : Globals.DataDirectory.listFiles()) {
                copyFile(file, new File(destDirPath + file.getName()));
            }
        } catch(IOException e) {
            Utils.messageBox(this, "Failed to export data", e.getMessage());
        }

        return true;
    }

    private void copyFile(File src, File dst) throws IOException {
        StringBuilder srcStr = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(src));
        while(reader.ready())
            srcStr.append(reader.readLine()).append('\n');
        reader.close();
        srcStr.deleteCharAt(srcStr.lastIndexOf("\n"));

        if(!dst.exists())
            dst.createNewFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(dst));
        writer.write(srcStr.toString());
        writer.close();
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
