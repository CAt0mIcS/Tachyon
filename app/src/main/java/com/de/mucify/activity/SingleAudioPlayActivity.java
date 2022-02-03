package com.de.mucify.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.mediarouter.app.MediaRouteButton;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.controller.SingleAudioPlayController;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Song;
import com.de.mucify.service.MediaSessionService;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class SingleAudioPlayActivity extends AppCompatActivity {
    private Intent mSongPlayForegroundIntent;

    private static SingleAudioPlayActivity sInstance = null;

    private CastContext mCastContext;
    private CastSession mCastSession;
    private SessionManagerListener<CastSession> mSessionManagerListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;

        setContentView(R.layout.song_loop_play_activity);

        mSongPlayForegroundIntent = new Intent(this, MediaSessionService.class);
        BottomNavigationView btmNav = findViewById(R.id.btmNav);

        // When switching to this activity, we need to know if we want the Song
        // or Loop menu item selected, default will be Song item.
        // Doing this before setting the listener, otherwise we would immediately switch back to
        // the song/loop/playlist select activity
        int navItemID = getIntent().getIntExtra("NavItemID", R.id.songs);
        btmNav.setSelectedItemId(navItemID);

        btmNav.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.songs:
                case R.id.loops:
                    Intent i = new Intent(SingleAudioPlayActivity.this, SingleAudioActivity.class);
                    i.putExtra("NavItemID", item.getItemId());
                    startActivity(i);
                    finish();
                    break;
                case R.id.playlists:
                    i = new Intent(SingleAudioPlayActivity.this, MultiAudioActivity.class);
                    startActivity(i);
                    finish();
                    break;
                case R.id.settings:
                    i = new Intent(SingleAudioPlayActivity.this, SettingsActivity.class);
                    startActivity(i);
                    finish();
                    break;

            }

            return true;
        });

        if(!getIntent().getBooleanExtra("PreserveAudio", false) || getIntent().hasExtra("EditLoop")) {
            try {
                AudioController.get().setSong(new Song(this, new File(getIntent().getStringExtra("AudioFilePath"))));
            } catch (Song.LoadingFailedException e) {
                e.printStackTrace();
            }

            AudioController.get().addOnSongUnpausedListener(song -> {
                startService(mSongPlayForegroundIntent);
            }, AudioController.INDEX_DONT_CARE);

            stopService(mSongPlayForegroundIntent);
            startService(mSongPlayForegroundIntent);

            AudioController.get().startSong();
        }

        // MY_TODO: Test if needs to be called in onResume?
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        MediaRouteButton mediaRouteButton = findViewById(R.id.media_route_button);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);

        setUpSessionManagerListener();
        mCastContext = CastContext.getSharedInstance(this);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
        setUpActionBar();

        mediaRouteButton.setOnClickListener(view -> {
            MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);

            movieMetadata.putString(MediaMetadata.KEY_TITLE, AudioController.get().getSongTitle());
            movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, AudioController.get().getSongArtist());
        });

        new SingleAudioPlayController(this);
        if(getIntent().hasExtra("EditLoop"))
            AudioController.get().pauseSong();
    }

    public int getNavItemID() {
        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        return btmNav.getSelectedItemId();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MucifyApplication.activityResumed(this);
        sInstance = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MucifyApplication.activityPaused(this);
    }

    public static SingleAudioPlayActivity get() {
        return sInstance;
    }


    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSessionManagerListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(CastSession session) {
            }

            @Override
            public void onSessionEnding(CastSession session) {
            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {
            }

            @Override
            public void onSessionSuspended(CastSession session, int reason) {
            }

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;
//                if (null != mSelectedMedia) {
//
//                    if (mPlaybackState == PlaybackState.PLAYING) {
//                        mVideoView.pause();
//                        loadRemoteMedia(mSeekbar.getProgress(), true);
//                        return;
//                    } else {
//                        mPlaybackState = PlaybackState.IDLE;
//                        updatePlaybackLocation(PlaybackLocation.REMOTE);
//                    }
//                }
//                updatePlayButton(mPlaybackState);
                invalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
//                updatePlaybackLocation(PlaybackLocation.LOCAL);
//                mPlaybackState = PlaybackState.IDLE;
//                mLocation = PlaybackLocation.LOCAL;
//                updatePlayButton(mPlaybackState);
                invalidateOptionsMenu();
            }
        };
    }
}
