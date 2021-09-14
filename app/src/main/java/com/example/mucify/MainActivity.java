package com.example.mucify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.mucify.program_objects.Song;
import com.example.mucify.ui.main.PlaylistFragment;
import com.example.mucify.ui.main.SingleSongFragment;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Choreographer;
import android.view.View;

import com.example.mucify.ui.main.SectionsPagerAdapter;
import com.example.mucify.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import kotlin.NotImplementedError;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private ArrayList<File> mAvailableSongs = new ArrayList<>();
    private ArrayList<File> mAvailableLoops = new ArrayList<>();
    public Song CurrentSong;

    private String mSongName = "Last Time - Nerxa.mp3";

    public File DataDirectory;
    public File MusicDirectory;

    public final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".3gp", ".mp4", ".m4a", ".aac", ".ts", ".amr", ".flac", ".ota", ".imy", ".mp3", ".mkv", ".ogg", ".wav");
    public final String LOOP_FILE_EXTENSION = ".txt";
    public final String LOOP_FILE_IDENTIFIER = "LOOP_";

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> mRequestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    throw new NotImplementedError();
                }
            });


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

        DataDirectory = new File(getDataDir().getPath() + "/files");
        MusicDirectory = new File("/storage/emulated/0/Music");  // MY_TODO: Shouldn't be hard coded

        LoadAvailableSongs(MusicDirectory);
        LoadAvailableLoops(DataDirectory);

        // Update Song
        Choreographer.FrameCallback callback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (CurrentSong != null)
                    CurrentSong.update();

                Choreographer.getInstance().postFrameCallback(this);
            }
        };
        Choreographer.getInstance().postFrameCallback(callback);

    }

    public void onSongOpen(View view) {
        // MY_TODO: Open window to select song
        String file = "/storage/emulated/0/Music/" + mSongName;

        if(!new File(file).exists())
            return;

        if(CurrentSong != null)
            CurrentSong.release();
        CurrentSong = new Song(this, file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")), file);
        CurrentSong.play(0, CurrentSong.getDuration());

        Objects.requireNonNull(getSingleSongFragment()).openSong(this);
    }

    public void onLoopLoad(View view) throws IOException {
        // MY_TODO: Open window to select loop
        String file = "LOOP_Ending_Last Time - Nerxa.txt";

        String loopName = file.split("_")[1];

        BufferedReader reader = new BufferedReader(new InputStreamReader(getApplicationContext().openFileInput(file)));
        String path = reader.readLine();
        int loopStartTime = Integer.parseInt(reader.readLine());
        int loopEndTime = Integer.parseInt(reader.readLine());
        reader.close();

        if(CurrentSong != null)
            CurrentSong.release();
        CurrentSong = new Song(getApplicationContext(), file.substring(file.lastIndexOf("_") + 1, file.indexOf(LOOP_FILE_EXTENSION)), path);
        CurrentSong.play(loopStartTime, loopEndTime);
    }

    public void onLoopSave(View view) throws IOException {
        // MY_TODO: Open window to configure name. Should not contain _
        String loopName = "Ending";

        if(CurrentSong == null)
            return;

        OutputStreamWriter writer = new OutputStreamWriter(
                getApplicationContext().openFileOutput(LOOP_FILE_IDENTIFIER + loopName + "_" + CurrentSong.Name + LOOP_FILE_EXTENSION, Context.MODE_PRIVATE));
        writer.write(CurrentSong.Path + "\n");  // Path to music file
        writer.write(CurrentSong.getStartTime() + "\n");  // Loop start time in seconds
        writer.write(CurrentSong.getEndTime() + "\n");  // Loop end time in seconds
        writer.close();
    }

    private Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".")));
    }

    private void LoadAvailableSongs(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {

                for (File file : files) {
                    if (file.isDirectory()) {
                        LoadAvailableSongs(file);
                    } else {
                        Optional<String> extension = getFileExtension(file.getName());
                        if(extension.isPresent() && SUPPORTED_EXTENSIONS.contains(extension.get()))
                            mAvailableSongs.add(file);
                    }
                }
            }
        }
    }

    private void LoadAvailableLoops(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {

                for (File file : files) {
                    if (file.isDirectory()) {
                        LoadAvailableLoops(file);
                    } else {
                        Optional<String> extension = getFileExtension(file.getName());
                        if(extension.isPresent() && extension.get().equals(LOOP_FILE_EXTENSION) && file.getName().indexOf(LOOP_FILE_IDENTIFIER) == 0)
                            mAvailableLoops.add(file);
                    }
                }
            }
        }
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
        if(viewPager.getCurrentItem() == 0 && page != null)
            return ((PlaylistFragment)page);

        return null;
    }
}