package com.de.mucify.ui;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class CastActivity extends AppCompatActivity {
    public enum PlaybackLocation {
        Remote, Local
    }

    private MenuItem mMediaRouteMenuItem;
    private PlaybackLocation mPlaybackLocation;
    private File mPlaybackPath;

    private WebServer mServer;

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
    protected void onDestroy() {
        super.onDestroy();
        mServer.stop();
    }

    /**
     * Takes the playback and stores it. If the user then wants to cast to a device, we'll need to
     * upload the local device file to a local server and have the receiver download it from there.
     * (https://stackoverflow.com/questions/32049851/it-is-posible-to-cast-or-stream-android-chromecast-a-local-file)
     */
    public void play(String mediaId) {
        mPlaybackPath = MediaLibrary.getPathFromMediaId(mediaId);
        try {
            mServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        Util.logGlobal( "Starting server: " + ip);
    }

    protected void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.inflateMenu(R.menu.toolbar_default);
        toolbar.setTitle(getString(R.string.library));
        mMediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), toolbar.getMenu(), R.id.media_route_menu_item);
    }

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
                supportInvalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                mPlaybackLocation = PlaybackLocation.Local;
                supportInvalidateOptionsMenu();
            }
        };
    }


    private class WebServer extends NanoHTTPD {
        private static final int PORT = 8080;

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

                return newChunkedResponse(Response.Status.OK, MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileManager.getFileExtension(mPlaybackPath.getPath())), fis);
            }

            return  null;
        }
    }
}
