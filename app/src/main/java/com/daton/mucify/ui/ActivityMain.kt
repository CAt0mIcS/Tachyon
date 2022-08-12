package com.daton.mucify.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.daton.media.MediaController
import com.daton.media.data.MediaAction
import com.daton.media.data.MediaId
import com.daton.media.device.BrowserTree
import com.daton.media.device.Loop
import com.daton.media.device.Song
import com.daton.mucify.R
import com.daton.mucify.databinding.ActivityMainBinding
import com.daton.mucify.permission.Permission
import com.daton.mucify.permission.PermissionManager
import com.daton.user.User
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch


class ActivityMain : AppCompatActivity() {
    companion object {
        const val TAG = "ActivityMain"
    }

    private var hasStoragePermission: Boolean = false

    private lateinit var binding: ActivityMainBinding
    private val mediaController = MediaController()

    private val historyStrings = mutableListOf<String>()

    /**
     * Counts down when the storage permission was either accepted or denied
     */
    private var permissionResultAvailable = CountDownLatch(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: When going into app info and setting files and media permission to "Media files only"
        // TODO: we need to use MediaStore to load files?
        PermissionManager.from(this).apply {
            request(Permission.ReadStorage)
            rationale(getString(R.string.storage_permission_rationale))
            checkPermission { result: Boolean ->
                if (result) {
                    hasStoragePermission = true
                    Log.i(TAG, "Storage permission granted")
                } else
                    Log.i(TAG, "Storage permission NOT granted")
                permissionResultAvailable.countDown()
            }
        }

        mediaController.create(this)
        mediaController.onConnected = { onConnected() }

        CoroutineScope(Dispatchers.IO).launch { User.create(this@ActivityMain) }

        /**
         * Send loops and playlists to service
         * TODO: Optimize this as [MediaSource] updates multiple times
         */
        User.onLogin {
            mediaController.sendLoops(User.metadata.loops)
            mediaController.sendPlaylists(User.metadata.playlists)
        }

        Log.d(TAG, "onCreate finished")
    }

    override fun onStart() {
        super.onStart()
        mediaController.connect(this)
    }

    override fun onStop() {
        super.onStop()
        mediaController.disconnect()
    }

    fun onConnected() {
        CoroutineScope(Dispatchers.IO).launch {
            // Wait for permission dialog to be accepted or denied
            permissionResultAvailable.await()

            // Notify service to load local device files
            if (hasStoragePermission) {
                if (!User.loggedIn) {
                    mediaController.sendLoops(User.metadata.loops)
                    mediaController.sendPlaylists(User.metadata.playlists)
                }

                mediaController.loadMediaSource()

                mediaController.subscribe(BrowserTree.ROOT) { items ->
                    setupUI(items)
                }
            }
        }
    }

    private fun setupUI(playbacks: List<MediaBrowserCompat.MediaItem>) {
        Log.d(TAG, "Setting up ui")

        binding.btnLogin.setOnClickListener { User.login(this) }
        binding.btnLogout.setOnClickListener { User.logout(this) }

        loadHistoryStrings()
        sendInformation()

        binding.rvHistory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            historyStrings
        )

        val playMedia = { mediaId: MediaId ->

            mediaController.mediaId = mediaId
            // Only play if not playlist or an underlying media id is given (play specific song in playlist)
            if (!mediaId.isPlaylist || mediaId.underlyingMediaId != null)
                mediaController.play()

            User.metadata.addHistory(mediaId)
            User.metadata.saveToLocal()
            User.uploadMetadata()

            if (mediaId.isPlaylist) {
                val intent = Intent(this@ActivityMain, ActivityPlaylistPlayer::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@ActivityMain, ActivityPlayer::class.java)
                startActivity(intent)
            }

        }

        binding.rvHistory.setOnItemClickListener { _, _, i, _ ->
            playMedia(User.metadata.history[i])
        }

        User.metadata.onHistoryChanged = {
            loadHistoryStrings()
            (binding.rvHistory.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        }

        binding.relLayoutSongs.setOnClickListener {
            val fragment = FragmentSelectAudio(playbacks)
            supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .add(R.id.fragment_container_view, fragment)
                .commit()

            fragment.onItemClicked = playMedia
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(this@ActivityMain, ActivitySettings::class.java)
            startActivity(intent)
        }
    }

    /**
     * Sends basic things like [User.metadata.combineDifferentPlaybackTypes] to the [MediaBrowserCompat]
     */
    private fun sendInformation() {
        val bundle = Bundle()
        bundle.putBoolean(
            MediaAction.CombinePlaybackTypes,
            User.metadata.combineDifferentPlaybackTypes
        )
        mediaController.sendCustomAction(MediaAction.CombinePlaybackTypesChanged, bundle)
    }

    private fun loadHistoryStrings() {
        historyStrings.clear()
        historyStrings.addAll(User.metadata.history.map {
            if (it.isSong) {
                val song = Song(it)
                "*song*" + song.title + " - " + song.artist
            } else if (it.isLoop) {
                val loop = Loop(it)
                val song = Song(loop.songMediaId)
                "*loop*" + loop.loopName + " - " + song.title + " - " + song.artist
            } else it.baseMediaId.source
        })
    }
}
