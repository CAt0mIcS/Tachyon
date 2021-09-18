package com.example.mucify.ui.main;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mucify.MainActivity;
import com.example.mucify.R;
import com.example.mucify.Util;
import com.example.mucify.program_objects.Playlist;
import com.example.mucify.program_objects.Song;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlaylistFragment extends Fragment {

    private MainActivity mActivity = null;
    private View mView;

    public static String PLAYLIST_IDENTIFIER = "PLAYLIST_";
    public static String PLAYLIST_EXTENSION = ".playlist";

    public Playlist CurrentPlaylist;

    // Playlist where the layout edit_playlist_layout was activated from
    private File mContextPlaylist;

    private final ArrayList<Song> mSongsToAddToPlaylist = new ArrayList<>();

    private final ArrayList<File> mAvailablePlaylists = new ArrayList<>();
    private boolean mProgressSeekbarUpdate = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playlist_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mView = view;
    }

    public void create(MainActivity activity) {
        if(mActivity != null)
            return;

        mActivity = activity;

        mView.findViewById(R.id.pf_btnCreate).setOnClickListener(this::onCreateNewPlaylistClicked);
        ((ListView)mView.findViewById(R.id.pf_lstboxPlaylists)).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Pair pair = openEditPlaylistWindow();
                View popupView = (View)pair.first;
                PopupWindow popupWindow = (PopupWindow)pair.second;

                if(popupView == null || popupWindow == null) {
                    Util.messageBox(mActivity, "Error", "Failed to open dialog to load loop");
                    return false;
                }

                mContextPlaylist = mAvailablePlaylists.get(position);

                popupView.findViewById(R.id.pf_btnEdit).setOnClickListener(v1 -> {
                    popupWindow.dismiss();
                    onEditPlaylistClicked(v1);
                });
                popupView.findViewById(R.id.pf_btnDelete).setOnClickListener(v1 -> {
                    popupWindow.dismiss();
                    onDeletePlaylistClicked(v1);
                });

                return false;
            }
        });
        ((SeekBar)mView.findViewById(R.id.pf_sbProgress)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long millis = progress % 1000;
                long second = (progress / 1000) % 60;
                long minute = (progress / (1000 * 60)) % 60;
                long hour = (progress / (1000 * 60 * 60)) % 24;

                ((TextView)mView.findViewById(R.id.pf_lblProgress)).setText(String.format("%02d:%02d:%02d.%d", hour, minute, second, millis));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mProgressSeekbarUpdate = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mProgressSeekbarUpdate = true;
                CurrentPlaylist.seekTo(seekBar.getProgress());
            }
        });
        ((ListView)mView.findViewById(R.id.pf_lstboxPlaylistSongs)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CurrentPlaylist.playSong(position);
            }
        });

        ((ListView) mView.findViewById(R.id.pf_lstboxPlaylists)).setOnItemClickListener(this::onPlayPlaylistClicked);

        mView.findViewById(R.id.pf_btnPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CurrentPlaylist != null) {
                    if(CurrentPlaylist.isPaused())
                        CurrentPlaylist.resume();
                    else
                        CurrentPlaylist.pause();
                }

            }
        });

        loadAvailablePlaylists(mActivity.DataDirectory);

        updatePlaylistListbox();
    }

    public void onCreateNewPlaylistClicked(View view) {
        ArrayList<String> availableSongs = new ArrayList<>();
        for(File file : mActivity.AvailableSongs) {
            availableSongs.add(file.getName().replace(Util.getFileExtension(file.getName()).get(), ""));
        }

        ArrayList<String> availableLoops = new ArrayList<>();
        for(File file : mActivity.AvailableLoops) {
            availableLoops.add(file.getName().replace(mActivity.LOOP_FILE_IDENTIFIER, "").replace(mActivity.LOOP_FILE_EXTENSION, ""));
        }

        Pair pair = openCreatePlaylistWindow(availableSongs, availableLoops);
        View createPlaylistView = (View)pair.first;
        PopupWindow createPlaylistWindow = (PopupWindow)pair.second;

        ((ListView)createPlaylistView.findViewById(R.id.pf_lstboxAvailableSongs)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView v = (CheckedTextView) view;
                if(v.isChecked()) {
                    File path = mActivity.AvailableSongs.get(position);
                    mSongsToAddToPlaylist.add(new Song(mActivity, path.getPath().substring(path.getPath().lastIndexOf("/") + 1, path.getPath().lastIndexOf(".")), path.getAbsolutePath()));
                    mSongsToAddToPlaylist.get(mSongsToAddToPlaylist.size() - 1).setEndTime(mSongsToAddToPlaylist.get(mSongsToAddToPlaylist.size() - 1).getDuration());
                }
            }
        });
        ((ListView)createPlaylistView.findViewById(R.id.pf_lstboxAvailableLoops)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView v = (CheckedTextView) view;
                if(v.isChecked()) {
                    File file = mActivity.AvailableLoops.get(position);

                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(mActivity.AvailableLoops.get(position)));
                        String path = reader.readLine();
                        int loopStartTime = Integer.parseInt(reader.readLine());
                        int loopEndTime = Integer.parseInt(reader.readLine());
                        reader.close();
                        mSongsToAddToPlaylist.add(new Song(mActivity, path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".")), path, loopStartTime, loopEndTime));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        createPlaylistView.findViewById(R.id.pf_btnCreatePlaylistOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSongsToAddToPlaylist.isEmpty())
                    return;

                if(CurrentPlaylist != null)
                    CurrentPlaylist.reset();

                String name = ((EditText)createPlaylistView.findViewById(R.id.pf_txtPlaylistName)).getText().toString();
                if(name.contains("_") || name.isEmpty()) {
                    Util.messageBox(mActivity, "Error", "Playlist name mustn't contain '_' or be empty");
                    return;
                }

                createPlaylistWindow.dismiss();

                Playlist playlist = new Playlist(mActivity, name, mSongsToAddToPlaylist);
                mSongsToAddToPlaylist.clear();
                try {
                    playlist.save();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                playlist.reset();

                File file = new File(mActivity.DataDirectory.getAbsolutePath() + "/" + PLAYLIST_IDENTIFIER + name + PLAYLIST_EXTENSION);
                if(!mAvailablePlaylists.contains(file)) {
                    mAvailablePlaylists.add(file);
                    updatePlaylistListbox();
                }
            }
        });


    }

    private void onPlayPlaylistClicked(AdapterView<?> adapterView, View view, int position, long l) {
        mView.findViewById(R.id.pf_lstboxPlaylists).setVisibility(View.INVISIBLE);
        mView.findViewById(R.id.pf_btnCreate).setVisibility(View.INVISIBLE);

        mView.findViewById(R.id.pf_btnClosePlaylist).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.pf_switchRandomizedPlay).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.pf_btnPause).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.pf_sbProgress).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.pf_lblProgress).setVisibility(View.VISIBLE);

        // Deactivate player
        mView.findViewById(R.id.pf_btnClosePlaylist).setOnClickListener(v -> {
            mView.findViewById(R.id.pf_lstboxPlaylists).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.pf_btnCreate).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.pf_btnClosePlaylist).setVisibility(View.INVISIBLE);
            mView.findViewById(R.id.pf_lstboxPlaylistSongs).setVisibility(View.INVISIBLE);
            mView.findViewById(R.id.pf_switchRandomizedPlay).setVisibility(View.INVISIBLE);
            mView.findViewById(R.id.pf_btnPause).setVisibility(View.INVISIBLE);
            mView.findViewById(R.id.pf_sbProgress).setVisibility(View.INVISIBLE);
            mView.findViewById(R.id.pf_lblProgress).setVisibility(View.INVISIBLE);

            if(CurrentPlaylist != null)
                CurrentPlaylist.reset();
        });

        ListView playlistSongs  = (ListView)mView.findViewById(R.id.pf_lstboxPlaylistSongs);
        playlistSongs.setVisibility(View.VISIBLE);

        File playlistFile = mAvailablePlaylists.get(position);
        String name = playlistFile.getName().replace(PLAYLIST_IDENTIFIER, "").replace(PLAYLIST_EXTENSION, "");

        if(CurrentPlaylist != null)
            CurrentPlaylist.reset();
        CurrentPlaylist = new Playlist(mActivity, name, playlistFile);

        playlistSongs.setAdapter(new ArrayAdapter<Song>(mActivity, android.R.layout.simple_list_item_1, CurrentPlaylist.Songs) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        });
    }

    public void onEditPlaylistClicked(View view) {
        if(mContextPlaylist == null)
            return;
    }

    public void onDeletePlaylistClicked(View view) {
        if(mContextPlaylist == null)
            return;

        if(mContextPlaylist.exists()) {
            if(mContextPlaylist.delete()) {
                mAvailablePlaylists.remove(mContextPlaylist);
                updatePlaylistListbox();
            }
            else
                Util.messageBox(mActivity, "Error", "Failed to delete file '" + mContextPlaylist.getAbsolutePath() + "'");
        }
    }


    private void updatePlaylistListbox() {
        ArrayList<String> playlists = new ArrayList<>();
        for(File file : mAvailablePlaylists)
            playlists.add(file.getName().replace(PLAYLIST_IDENTIFIER, "").replace(PLAYLIST_EXTENSION, ""));

        ((ListView)mView.findViewById(R.id.pf_lstboxPlaylists)).setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, playlists) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        });
    }

    private void loadAvailablePlaylists(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {

                for (File file : files) {
                    if (file.isDirectory()) {
                        loadAvailablePlaylists(file);
                    } else {
                        Optional<String> extension = Util.getFileExtension(file.getName());
                        if(extension.isPresent() && extension.get().equals(PLAYLIST_EXTENSION) && file.getName().indexOf(PLAYLIST_IDENTIFIER) == 0)
                            mAvailablePlaylists.add(file);
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private android.util.Pair openEditPlaylistWindow() {
        // Create Dialog for loop name
        LayoutInflater inflater = (LayoutInflater)
                mActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.playlist_context_menu_layout, null);
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

        return new android.util.Pair<>(popupView, popupWindow);
    }

    @SuppressLint("ClickableViewAccessibility")
    private android.util.Pair openCreatePlaylistWindow(List<String> availableSongs, List<String> availableLoops) {
        // Create Dialog for loop name
        LayoutInflater inflater = (LayoutInflater)
                mActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.playlist_create_fragment, null);
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

        ((ListView)popupView.findViewById(R.id.pf_lstboxAvailableSongs)).setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_multiple_choice, availableSongs) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        });
        ((ListView)popupView.findViewById(R.id.pf_lstboxAvailableLoops)).setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_multiple_choice, availableLoops) {
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

    public void update() {
        if(CurrentPlaylist != null) {
            CurrentPlaylist.update(((Switch)mView.findViewById(R.id.pf_switchRandomizedPlay)).isChecked());
            SeekBar seekBar = ((SeekBar)mView.findViewById(R.id.pf_sbProgress));
            seekBar.setMax(CurrentPlaylist.getDuration());
            if(mProgressSeekbarUpdate)
                seekBar.setProgress(CurrentPlaylist.getCurrentPosition());
        }
    }
}