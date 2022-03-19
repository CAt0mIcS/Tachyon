package com.de.mucify.ui;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.media.MediaMetadataCompat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.de.mucify.FileManager;
import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.Util;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public abstract class CastActivity extends AppCompatActivity {
    public enum PlaybackLocation {
        Remote, Local
    }

    private MenuItem mMediaRouteMenuItem;
    private PlaybackLocation mPlaybackLocation;
    private File mPlaybackPath;

    private WebServer mServer;
    private String mIP;

    /**
     * Cache MIME type because it takes a while for it to load
     */
    private String mMIMEType;

    private CastContext mCastContext;
    private CastSession mCastSession;
    private SessionManagerListener<CastSession> mSessionManagerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupCastListener();

        mCastContext = CastContext.getSharedInstance(this);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
        mServer = new WebServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);
        Log.d("Mucify", "CastActivity.onResume");
    }

    /**
     * Takes the playback and stores it. If the user then wants to cast to a device, we'll need to
     * upload the local device file to a local server and have the receiver download it from there.
     * (https://stackoverflow.com/questions/32049851/it-is-posible-to-cast-or-stream-android-chromecast-a-local-file)
     */
    public void play(String mediaId) {
        mPlaybackPath = MediaLibrary.getPathFromMediaId(mediaId);

        // Guess the MIME type of the playback, remove the . in the file extension
        mMIMEType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileManager.getFileExtension(mPlaybackPath.getPath()).substring(1));
    }

    public abstract void pause();
    public abstract boolean isPlaying();
    public abstract int getCurrentPosition();
    public abstract int getDuration();
    public abstract String getSongTitle();
    public abstract String getSongArtist();


    /**
     * Uses the CastButtonFactory to initialize the media route menu item. Call this in any activity
     * after calling setContentIntent(@LayoutId int) that should have a cast button. Requires the layout
     * to have a toolbar with id equal to my_toolbar
     */
    protected void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.inflateMenu(R.menu.toolbar_default);
        toolbar.setTitle(getString(R.string.library));
        mMediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), toolbar.getMenu(), R.id.media_route_menu_item);
    }

    /**
     * Assigns mSessionManagerListener and implements methods to listen to application connect
     * and disconnect callbacks from the cast receiver.
     */
    private void setupCastListener() {
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
            public void onSessionStarting(CastSession session) {}

            @Override
            public void onSessionEnding(CastSession session) {}

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {}

            @Override
            public void onSessionSuspended(CastSession session, int reason) {}

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;
                mPlaybackLocation = PlaybackLocation.Remote;
                startServer();

                if (isPlaying()) {
                    pause();
                    loadRemoteMedia(getCurrentPosition(), true);
                    return;
                }

                supportInvalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                mPlaybackLocation = PlaybackLocation.Local;
                mServer.stop();
                Util.logGlobal("Stopping Cast server");
                supportInvalidateOptionsMenu();
            }
        };
    }

    /**
     * Called when we start casting, sends audio to cast receiver
     */
    private void loadRemoteMedia(int seekPos, boolean autoPlay) {
        if (mCastSession == null) {
            return;
        }
        final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }

//        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
//            @Override
//            public void onStatusUpdated() {
//                Intent intent = new Intent(LocalPlayerActivity.this, ExpandedControlsActivity.class);
//                startActivity(intent);
//                remoteMediaClient.unregisterCallback(this);
//            }
//        });
        remoteMediaClient.load(new MediaLoadRequestData.Builder()
                .setMediaInfo(buildMediaInfo())
                .setAutoplay(autoPlay)
                .setCurrentTime(seekPos)
                .build());
    }

    /**
     * @return information about the media we're about to play
     */
    private MediaInfo buildMediaInfo() {
        MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        metadata.putString(MediaMetadata.KEY_TITLE, getSongTitle());
        metadata.putString(MediaMetadata.KEY_ARTIST, getSongArtist());
        metadata.putString(MediaMetadata.KEY_SUBTITLE, getSongArtist());

        String url = "http://" + mIP + ":" + WebServer.PORT + "/audio";
        Log.d("Mucify", "Loading Cast MediaInfo with URL: " + url);
        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(mMIMEType)
                .setMetadata(metadata)
                .setStreamDuration(getDuration())
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

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        Util.logGlobal( "Starting Cast server: " + mIP);
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
            Log.i("Mucify.CastWebServer", "Serve: "+ session.getUri());
            String uri = session.getUri();

            if (uri.equals("/audio")) {

                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(mPlaybackPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return newChunkedResponse(Response.Status.OK, mMIMEType, fis);
            }

            return  null;
        }
    }
}
