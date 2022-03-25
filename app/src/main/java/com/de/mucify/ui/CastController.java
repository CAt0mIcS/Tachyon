package com.de.mucify.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.de.mucify.FileManager;
import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.UserData;
import com.de.mucify.Util;
import com.de.mucify.player.Playback;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;

import fi.iki.elonen.NanoHTTPD;

public class CastController implements IMediaController {

    public enum PlaybackLocation {
        Remote, Local
    }

    private MenuItem mMediaRouteMenuItem;
    private PlaybackLocation mPlaybackLocation;
    private Playback mPlayback;
    private static final Handler mPlaybackUpdateHandler = new Handler();
    private final Runnable mPlaybackUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
            // MY_TODO: Test with loops and playlists
            synchronized (mCastSessionLock) {
                if(mCastSession == null) {
                    Util.logGlobal("CastSession is null");
                    return;
                }

                Util.logGlobal("Restarting casting playback ms" + getCurrentPosition());
                play(mPlayback.getMediaId());
            }
        }
    };

    private final WebServer mServer = new WebServer();
    private String mIP;

    /**
     * Cache MIME type because it takes a while for it to load
     */
    private String mMIMEType;

    private final CastContext mCastContext;
    private CastSession mCastSession;
    private final Object mCastSessionLock = new Object();
    private SessionManagerListener<CastSession> mSessionManagerListener;

    private final MediaControllerActivity mActivity;
    private final ArrayList<MediaControllerActivity.Callback> mCallbacks;


    public CastController(MediaControllerActivity activity, ArrayList<MediaControllerActivity.Callback> callbacks) {
        mActivity = activity;
        mCallbacks = callbacks;
        setupCastListener();

        mCastContext = CastContext.getSharedInstance(mActivity);
        synchronized (mCastSessionLock) {
            mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
        }
    }

    public void onResume() {
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);
        Log.d("Mucify", "CastController.onResume");
    }

    @Override
    public void unpause() {
        synchronized (mCastSessionLock) {
            mCastSession.getRemoteMediaClient().play();
        }

        for(MediaControllerActivity.Callback c : mCallbacks)
            c.onStart();
    }

    @Override
    public void pause() {
        synchronized (mCastSessionLock) {
            mCastSession.getRemoteMediaClient().pause();
        }

        for(MediaControllerActivity.Callback c : mCallbacks)
            c.onPause();
    }

    @Override
    public void seekTo(int millis) {
        synchronized (mCastSessionLock) {
            mCastSession.getRemoteMediaClient().seek(new MediaSeekOptions.Builder()
                    .setPosition(millis)
                    .build());
        }

        for(MediaControllerActivity.Callback c : mCallbacks)
            c.onSeekTo(millis);

        mPlaybackUpdateHandler.removeCallbacks(mPlaybackUpdateRunnable);
//        mPlaybackUpdateHandler.postDelayed(mPlaybackUpdateRunnable, mPlayback.getCurrentSong().getEndTimeUninitialized() - getCurrentPosition());
        mPlaybackUpdateHandler.postDelayed(mPlaybackUpdateRunnable, 10);
    }


    @Override
    public void play(String mediaId) {
        mPlayback = MediaLibrary.getPlaybackFromMediaId(mediaId);

        synchronized (mCastSessionLock) {
            loadRemoteMedia(mCastSession.getRemoteMediaClient(), 0, true);
        }
        mPlaybackLocation = PlaybackLocation.Remote;

        for(MediaControllerActivity.Callback c : mCallbacks)
            c.onStart();

        mPlaybackUpdateHandler.removeCallbacks(mPlaybackUpdateRunnable);
        int delay = mPlayback.getCurrentSong().getEndTimeUninitialized() - 5;
        Log.d("Mucify.Cast", "Posting cast handler with delay ms" + delay);
        mPlaybackUpdateHandler.postDelayed(mPlaybackUpdateRunnable, delay);

        Util.logGlobal("CastController.play " + mediaId + " ms" + delay);
    }

    @Override
    public boolean isPlaying() {
        synchronized (mCastSessionLock) {
            return mCastSession.getRemoteMediaClient().isPlaying();
        }
    }

    @Override
    public boolean isCreated() {
        synchronized (mCastSessionLock) {
            return mCastSession != null && mCastSession.getRemoteMediaClient() != null;
        }
    }

    @Override
    public boolean isPaused() {
        synchronized (mCastSessionLock) {
            return mCastSession.getRemoteMediaClient().isPaused();
        }
    }

    @Override
    public void setStartTime(int millis) {

    }

    @Override
    public void setEndTime(int millis) {

    }

    @Override
    public int getStartTime() {
        return 0;
    }

    @Override
    public int getEndTime() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        synchronized (mCastSessionLock) {
            return (int) mCastSession.getRemoteMediaClient().getApproximateStreamPosition();
        }
    }

    @Override
    public int getDuration() {
        synchronized (mCastSessionLock) {
            return (int) mCastSession.getRemoteMediaClient().getStreamDuration();
        }
    }

    @Override
    public String getSongTitle() {
        return getMetadata().getString(MediaMetadata.KEY_TITLE);
    }

    @Override
    public String getSongArtist() {
        return getMetadata().getString(MediaMetadata.KEY_ARTIST);
    }


    public MediaMetadata getMetadata() {
        synchronized (mCastSessionLock) {
            return mCastSession.getRemoteMediaClient().getMediaInfo().getMetadata();
        }
    }

    public boolean isCasting() {
        synchronized (mCastSessionLock) {
            return mCastSession != null && mCastSession.isConnected();
        }
    }


    /**
     * Uses the CastButtonFactory to initialize the media route menu item. Call this in any activity
     * after calling setContentIntent(@LayoutId int) that should have a cast button. Requires the layout
     * to have a toolbar with id equal to my_toolbar
     */
    protected void initializeToolbar() {
        Toolbar toolbar = mActivity.findViewById(R.id.my_toolbar);
        toolbar.inflateMenu(R.menu.toolbar_default);
        toolbar.setTitle(mActivity.getString(R.string.library));
        mMediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(mActivity.getApplicationContext(), toolbar.getMenu(), R.id.media_route_menu_item);
        toolbar.findViewById(R.id.action_settings).setOnClickListener(v -> {
            Intent i = new Intent(mActivity, ActivitySettings.class);
            mActivity.startActivity(i);
        });
    }

    /**
     * Takes the playback and stores it. If the user then wants to cast to a device, we'll need to
     * upload the local device file to a local server and have the receiver download it from there.
     * (https://stackoverflow.com/questions/32049851/it-is-posible-to-cast-or-stream-android-chromecast-a-local-file)
     */
    public void setPlayback(String mediaId) {
        mPlayback = MediaLibrary.getPlaybackFromMediaId(mediaId);

        // Guess the MIME type of the playback, remove the . in the file extension
        mMIMEType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileManager.getFileExtension(
                mPlayback.getCurrentSong().getSongPath().getPath()).substring(1));
    }

    /**
     * Assigns mSessionManagerListener and implements methods to listen to application connect
     * and disconnect callbacks from the cast receiver.
     */
    private void setupCastListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(@NonNull CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(@NonNull CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(@NonNull CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(@NonNull CastSession session, @NonNull String sessionId) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(@NonNull CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(@NonNull CastSession session) {}

            @Override
            public void onSessionEnding(@NonNull CastSession session) {
                // MY_TODO: mCurrentSession might be null here
//                UserData.PlaybackInfos.get(UserData.PlaybackInfos.size() - 1).PlaybackPos = getCurrentPosition();
//                UserData.save();
            }

            @Override
            public void onSessionResuming(@NonNull CastSession session, @NonNull String sessionId) {}

            @Override
            public void onSessionSuspended(@NonNull CastSession session, int reason) {}

            private void onApplicationConnected(CastSession castSession) {
                startServer();
                synchronized (mCastSessionLock) {
                    mCastSession = castSession;

                    mCastSession.getRemoteMediaClient().registerCallback(new RemoteMediaClient.Callback() {
                        @Override
                        public void onStatusUpdated() {
//                        mCastSession.getRemoteMediaClient().getMediaStatus()
                            if (isPaused()) {
                                for (MediaControllerActivity.Callback c : mCallbacks)
                                    c.onPause();
                            } else if (isPlaying()) {
                                for (MediaControllerActivity.Callback c : mCallbacks)
                                    c.onStart();
                            }
                        }

                        @Override
                        public void onMetadataUpdated() {
                            MediaInfo mediaInfo = mCastSession.getRemoteMediaClient().getMediaInfo();
                            Util.logGlobal("RemoteMediaClient.Callback.onMetadataUpdated");
                        }
                    });
                }

                mActivity.supportInvalidateOptionsMenu();
                for(MediaControllerActivity.Callback c : mCallbacks)
                    c.onCastConnected();
            }

            private void onApplicationDisconnected() {
                synchronized (mCastSessionLock) {
                    mCastSession = null;
                }
                mPlaybackLocation = PlaybackLocation.Local;
                mServer.stop();
                Log.i("Mucify", "Stopping Cast server");
                mActivity.supportInvalidateOptionsMenu();

                // Playback is paused when cast ends
                for(MediaControllerActivity.Callback c : mCallbacks) {
                    c.onCastDisconnected();
                    c.onPause();
                }

            }
        };
    }

    /**
     * Called when we start casting, sends audio to cast receiver
     */
    private void loadRemoteMedia(RemoteMediaClient remoteClient, int seekPos, boolean autoPlay) {
        if(remoteClient == null) {
            throw new UnsupportedOperationException("Remote media client is null");
        }

        remoteClient.load(new MediaLoadRequestData.Builder()
                .setMediaInfo(buildMediaInfo())
                .setAutoplay(autoPlay)
                .setCurrentTime(seekPos)
                .build());
    }

    /**
     * @return information about the media we're about to play
     */
    private MediaInfo buildMediaInfo() {
        String title = mPlayback.getTitle();
        String subtitle = mPlayback.getSubtitle();
        int duration = mPlayback.getCurrentSong().getDurationUninitialized();

        String urlBase = "http://" + mIP + ":" + WebServer.PORT;
        String urlAudio = urlBase + "/audio";
        String urlImageLow = urlBase + "/image_low";
        String urlImageHigh = urlBase + "/image_high";

        MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        metadata.putString(MediaMetadata.KEY_TITLE, title);
        metadata.putString(MediaMetadata.KEY_ARTIST, subtitle);

        if(mPlayback.getCurrentSong().getImage() != null)
            metadata.addImage(new WebImage(Uri.parse(urlImageLow)));

        Util.logGlobal("Building Metadata with " + title + " " + subtitle + " ms" + duration);
        Util.logGlobal("Loading Cast MediaInfo with URLAudio: " + urlAudio + "\nURLImageLow: " + urlImageLow);
        return new MediaInfo.Builder(urlAudio)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(mMIMEType)
                .setMetadata(metadata)
                .setStreamDuration(duration)
                .build();
    }

    /**
     * Starts the server with which the receiver will download the audio
     */
    private void startServer() {
        try {
            mServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mIP = Util.getIPAddress(mActivity);
        if(mIP == null) {
            // MY_TODO: Tell user to connect to WIFI
            throw new UnsupportedOperationException("Failed to get IP address");
        }
        Log.i("Mucify", "Starting Cast server: " + mIP);
    }


    /**
     * We can't cast device files without giving the receiver a url. Thus we create a local
     * HTTP server from which the receiver can download the audio.
     */
    private class WebServer extends NanoHTTPD {
        public static final int PORT = 8080;

        public WebServer() {
            super(PORT);
        }

        @Override
        public Response serve(IHTTPSession session) {
            Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
            Log.i("Mucify.CastWebServer", "Serve: "+ session.getUri());
            String uri = session.getUri();

            if (uri.equals("/audio")) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(mPlayback.getCurrentSong().getSongPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return newChunkedResponse(Response.Status.OK, mMIMEType, fis);
            }
            else if(uri.equals("/image_high")) {
                Bitmap imageData = mPlayback.getCurrentSong().getImage();
                return newFixedLengthResponse(Response.Status.OK, "image/png", bitmapToString(imageData, 100));
            }
            else if(uri.equals("/image_low")) {
                Bitmap imageData = mPlayback.getCurrentSong().getImage();
                return newFixedLengthResponse(Response.Status.OK, "image/png", bitmapToString(imageData, 25));
            }

            return  null;
        }

        public String bitmapToString(Bitmap bitmap, int quality) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, stream);
            return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
        }

        public Bitmap StringToBitMap(String encodedString){
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        }
    }
}
