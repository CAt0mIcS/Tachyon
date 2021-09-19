package com.example.mucify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.mucify.program_objects.Playlist;
import com.example.mucify.program_objects.Song;
import com.example.mucify.ui.main.PlaylistFragment;
import com.example.mucify.ui.main.SingleSongFragment;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.Settings;
import android.util.Pair;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.mucify.ui.main.SectionsPagerAdapter;
import com.example.mucify.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import kotlin.NotImplementedError;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;

    public Song CurrentSong;

    private Playlist mCurrentPlaylist;

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private final ActivityResultLauncher<String> mRequestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    throw new NotImplementedError();
                }
            });


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = mBinding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = mBinding.tabs;
        tabs.setupWithViewPager(viewPager);

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED) {
            mRequestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED) {
            mRequestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }

        try {
            GlobalConfig.load(this);
        } catch (IOException e) {
            e.printStackTrace();
            Util.messageBox(this, "Failed to load config", e.getMessage());
        }

        // Update Song
        Choreographer.FrameCallback callback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (CurrentSong != null)
                    CurrentSong.loopedUpdate();

                // MY_TODO: Android is a mess! How do I detect when the displayed fragment changes
                PlaylistFragment playlistFragment = getPlaylistFragment();
                SingleSongFragment singleSongFragment = getSingleSongFragment();
                if(playlistFragment != null) {
                    playlistFragment.create(MainActivity.this);
                    mCurrentPlaylist = playlistFragment.CurrentPlaylist;
                    if(CurrentSong != null)
                        CurrentSong.pause();

                    playlistFragment.update();
                }
                else if(singleSongFragment != null) {
                    if(mCurrentPlaylist != null) {
                        mCurrentPlaylist.reset();
                        mCurrentPlaylist = null;

                        findViewById(R.id.pf_btnClosePlaylist).performClick();
                    }
                }


                Choreographer.getInstance().postFrameCallback(this);
            }
        };
        Choreographer.getInstance().postFrameCallback(callback);

    }

    public void onSongOpen(View view) {
        ArrayList<String> filenames = new ArrayList<>();
        for(File file : GlobalConfig.AvailableSongs)
            filenames.add(file.getName());

        Pair pair = openOpenFileLayout(filenames);
        View openFileView = (View)pair.first;
        PopupWindow openFileWindow = (PopupWindow)pair.second;

        if(openFileView == null || openFileWindow == null) {
            Util.messageBox(this, "Error", "Failed to open file dialog to open a song");
            return;
        }

        ((ListView)openFileView.findViewById(R.id.lstboxFiles)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openFileWindow.dismiss();
                String file = GlobalConfig.AvailableSongs.get(position).getPath();

                if(!new File(file).exists()) {
                    Util.messageBox(MainActivity.this, "Error", "File '" + file + "' not found. Unable to open song");
                    return;
                }

                if(CurrentSong != null)
                    CurrentSong.reset();
                CurrentSong = new Song(MainActivity.this, file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")), file);
                CurrentSong.play(0, CurrentSong.getDuration());

                Objects.requireNonNull(getSingleSongFragment()).openSong(MainActivity.this);
            }
        });
    }

    public void onLoopLoad(View view) {
        ArrayList<String> filenames = new ArrayList<>();
        for(File file : GlobalConfig.AvailableLoops)
            filenames.add(file.getName().replace(GlobalConfig.LoopFileIdentifier, "").replace(GlobalConfig.LoopFileExtension, "").replaceFirst("_", " | "));

        Pair pair = openOpenFileLayout(filenames);
        View openFileView = (View)pair.first;
        PopupWindow openFileWindow = (PopupWindow)pair.second;

        if(openFileView == null || openFileWindow == null) {
            Util.messageBox(this, "Error", "Failed to open dialog to load loop");
            return;
        }

        ((ListView)openFileView.findViewById(R.id.lstboxFiles)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openFileWindow.dismiss();
                String file = GlobalConfig.AvailableLoops.get(position).getName();

                String path = null;
                int loopStartTime = 0;
                int loopEndTime = 0;
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(GlobalConfig.AvailableLoops.get(position)));
                    path = reader.readLine();
                    loopStartTime = Integer.parseInt(reader.readLine());
                    loopEndTime = Integer.parseInt(reader.readLine());
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Util.messageBox(MainActivity.this, "Error", e.getMessage());
                }

                if(CurrentSong != null)
                    CurrentSong.reset();
                CurrentSong = new Song(getApplicationContext(), file.substring(file.lastIndexOf("_") + 1, file.indexOf(GlobalConfig.LoopFileExtension)), path);
                CurrentSong.play(loopStartTime, loopEndTime);
                Objects.requireNonNull(getSingleSongFragment()).openSong(MainActivity.this);
            }
        });

        ((ListView)openFileView.findViewById(R.id.lstboxFiles)).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // MY_TODO: Alert the user that they're about to delete a loop
                File file = GlobalConfig.AvailableLoops.get(position);
                boolean result = file.delete();
                GlobalConfig.AvailableLoops.clear();
                GlobalConfig.loadAvailableLoops(GlobalConfig.DataDirectory);
                openFileWindow.dismiss();
                return true;
            }
        });
//        String loopName = file.split("_")[1];
    }

    public void onLoopSave(View view) {
        if(CurrentSong == null)
            return;

        // Create Dialog for loop name
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.save_loop_layout, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });
        popupView.findViewById(R.id.ssf_btnSaveLoop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loopName = ((EditText)popupView.findViewById(R.id.ssf_txtSaveLoop)).getText().toString();

                if(!loopName.isEmpty() && !loopName.contains("_")) {
                    try {
                        OutputStreamWriter writer = new OutputStreamWriter(
                                getApplicationContext().openFileOutput(GlobalConfig.LoopFileIdentifier + loopName + "_" + CurrentSong.Name + GlobalConfig.LoopFileExtension, Context.MODE_PRIVATE));
                        writer.write(CurrentSong.Path + "\n");  // Path to music file
                        writer.write(CurrentSong.getStartTime() + "\n");  // Loop start time in seconds
                        writer.write(CurrentSong.getEndTime() + "\n");  // Loop end time in seconds
                        writer.close();
                        popupWindow.dismiss();
                        GlobalConfig.AvailableLoops.clear();
                        GlobalConfig.loadAvailableLoops(GlobalConfig.DataDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Util.messageBox(MainActivity.this, "Error", e.getMessage());
                    }
                }
            }
        });
    }

    public void onPausePlayClick(View view) {
        if(CurrentSong != null) {
            if(CurrentSong.isPlaying())
                CurrentSong.pause();
            else
                CurrentSong.play();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private android.util.Pair openOpenFileLayout(List<String> items) {
        if(items.isEmpty())
            return new android.util.Pair<>(null, null);

        // Create Dialog for loop name
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.open_file_layout, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });

        ((ListView)popupView.findViewById(R.id.lstboxFiles)).setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        });

        return new android.util.Pair<>(popupView, popupWindow);
    }

    private SingleSongFragment getSingleSongFragment() {
        ViewPager viewPager = findViewById(R.id.view_pager);
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
        if(viewPager.getCurrentItem() == 0 && page != null)
            return ((SingleSongFragment)page);

        return null;
    }

    private PlaylistFragment getPlaylistFragment() {
        ViewPager viewPager = findViewById(R.id.view_pager);
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
        if(viewPager.getCurrentItem() == 1 && page != null)
            return ((PlaylistFragment)page);

        return null;
    }
}