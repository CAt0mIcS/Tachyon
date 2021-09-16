package com.example.mucify;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.mucify.program_objects.Song;
import com.example.mucify.ui.main.PlaylistFragment;
import com.example.mucify.ui.main.SingleSongFragment;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Pair;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import kotlin.NotImplementedError;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private final ArrayList<File> mAvailableSongs = new ArrayList<>();
    private final ArrayList<File> mAvailableLoops = new ArrayList<>();
    public Song CurrentSong;

    public File DataDirectory;
    public File MusicDirectory;

    public final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".3gp", ".mp4", ".m4a", ".aac", ".ts", ".amr", ".flac", ".ota", ".imy", ".mp3", ".mkv", ".ogg", ".wav");
    public final String LOOP_FILE_EXTENSION = ".txt";
    public final String LOOP_FILE_IDENTIFIER = "LOOP_";

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

        DataDirectory = new File(getDataDir().getPath() + "/files");
//        MusicDirectory = new File("/storage/emulated/0/Music");  // MY_TODO: Shouldn't be hard coded
        MusicDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");

        loadAvailableSongs(MusicDirectory);
        loadAvailableLoops(DataDirectory);

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
        ArrayList<String> filenames = new ArrayList<>();
        for(File file : mAvailableSongs)
            filenames.add(file.getName());

        Pair pair = openOpenFileLayout(filenames);
        View openFileView = (View)pair.first;
        PopupWindow openFileWindow = (PopupWindow)pair.second;

        if(openFileView == null || openFileWindow == null) {
            messageBox("Error", "Failed to open file dialog to open a song");
            return;
        }

        ((ListView)openFileView.findViewById(R.id.lstboxFiles)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openFileWindow.dismiss();
                String file = mAvailableSongs.get(position).getPath();

                if(!new File(file).exists()) {
                    messageBox("Error", "File '" + file + "' not found. Unable to open song");
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
        for(File file : mAvailableLoops)
            filenames.add(file.getName().replace(LOOP_FILE_IDENTIFIER, "").replace(LOOP_FILE_EXTENSION, ""));

        Pair pair = openOpenFileLayout(filenames);
        View openFileView = (View)pair.first;
        PopupWindow openFileWindow = (PopupWindow)pair.second;

        if(openFileView == null || openFileWindow == null) {
            messageBox("Error", "Failed to open dialog to load loop");
            return;
        }

        ((ListView)openFileView.findViewById(R.id.lstboxFiles)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openFileWindow.dismiss();
                String file = mAvailableLoops.get(position).getName();

                String path = null;
                int loopStartTime = 0;
                int loopEndTime = 0;
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(mAvailableLoops.get(position)));
                    path = reader.readLine();
                    loopStartTime = Integer.parseInt(reader.readLine());
                    loopEndTime = Integer.parseInt(reader.readLine());
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    messageBox("Error", e.getMessage());
                }

                if(CurrentSong != null)
                    CurrentSong.reset();
                CurrentSong = new Song(getApplicationContext(), file.substring(file.lastIndexOf("_") + 1, file.indexOf(LOOP_FILE_EXTENSION)), path);
                CurrentSong.play(loopStartTime, loopEndTime);
                Objects.requireNonNull(getSingleSongFragment()).openSong(MainActivity.this);
            }
        });

        ((ListView)openFileView.findViewById(R.id.lstboxFiles)).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // MY_TODO: Alert the user that they're about to delete a loop
                File file = mAvailableLoops.get(position);
                boolean result = file.delete();
                mAvailableLoops.clear();
                loadAvailableLoops(DataDirectory);
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
                                getApplicationContext().openFileOutput(LOOP_FILE_IDENTIFIER + loopName + "_" + CurrentSong.Name + LOOP_FILE_EXTENSION, Context.MODE_PRIVATE));
                        writer.write(CurrentSong.Path + "\n");  // Path to music file
                        writer.write(CurrentSong.getStartTime() + "\n");  // Loop start time in seconds
                        writer.write(CurrentSong.getEndTime() + "\n");  // Loop end time in seconds
                        writer.close();
                        popupWindow.dismiss();
                        mAvailableLoops.clear();
                        loadAvailableLoops(DataDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                        messageBox("Error", e.getMessage());
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

    private void messageBox(String title, String msg) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(msg);
        dlgAlert.setTitle(title);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".")));
    }

    private void loadAvailableSongs(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                 if(files.length == 0)
                     messageBox("Error", "No songs available to load in '" + dir.getAbsolutePath() + "'");

                for (File file : files) {
                    if (file.isDirectory()) {
                        loadAvailableSongs(file);
                    } else {
                        Optional<String> extension = getFileExtension(file.getName());
                        if(extension.isPresent() && SUPPORTED_EXTENSIONS.contains(extension.get()))
                            mAvailableSongs.add(file);
                    }
                }
            }
            else
                messageBox("Error", "Failed to load available songs from '" + dir.getAbsolutePath() + "'");
        }
    }

    private void loadAvailableLoops(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {

                for (File file : files) {
                    if (file.isDirectory()) {
                        loadAvailableLoops(file);
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