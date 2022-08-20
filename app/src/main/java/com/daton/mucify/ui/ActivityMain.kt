package com.daton.mucify.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.daton.media.MediaController
import com.daton.media.data.MediaAction
import com.daton.media.device.*
import com.daton.media.playback.Loop
import com.daton.media.playback.Playback
import com.daton.media.playback.Playlist
import com.daton.media.playback.Song
import com.daton.mucify.R
import com.daton.mucify.databinding.ActivityMainBinding
import com.daton.mucify.permission.Permission
import com.daton.mucify.permission.PermissionManager
import com.daton.user.User
import com.daton.util.launch
import kotlinx.coroutines.*


class ActivityMain : AppCompatActivity(),
    MediaController.IEventListener by MediaController.EventListener(),
    User.IEventListener by User.EventListener() {
    companion object {
        const val TAG = "ActivityMain"

        // TODO: ViewBinding
        private var mediaLoaded = false
    }

    private var hasStoragePermission: Boolean = false

    private lateinit var binding: ActivityMainBinding
    private val mediaController = MediaController()

    private val permissionResultAvailable = Job()

    private val historyStrings = mutableListOf<String>()

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
                permissionResultAvailable.complete()
            }
        }

        mediaController.create(this)
        mediaController.registerEventListener(this)

        User.create()
        User.registerEventListener(this)

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

    override fun onConnected() {
        launch(Dispatchers.IO) {
            // Wait for permission dialog to be accepted or denied
            permissionResultAvailable.join()

            // Notify service to load local device files
            if (hasStoragePermission) {

                if (!mediaLoaded) {
                    mediaController.loadMediaSource()
                    mediaLoaded = true
                }

                withContext(Dispatchers.Main) {
                    setupUI()
                }
            }
        }
    }

    // TODO: When first opening a freshly installed app the [MediaSource] loads and sets the state to
    // TODO: STATE_INITIALIZED, but [setupUI] is never called after that

    override fun onMetadataChanged() {
        launch(Dispatchers.IO) {
            mediaController.awaitConnection()
            Log.d(TAG, "Sending new loops and playlist to the MediaBrowserServiceCompat")
            mediaController.sendLoops(User.metadata.loops)
            mediaController.sendPlaylists(User.metadata.playlists)
        }
    }

    private fun setupUI() {
        Log.d(TAG, "Setting up ui")

        binding.btnLogin.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ActivitySignIn::class.java
                )
            )
        }
        binding.btnLogout.setOnClickListener { User.signOut() }

        loadHistoryStrings()
        sendInformation()

        binding.rvHistory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            historyStrings
        )

        val playMedia = { playback: Playback ->

            mediaController.playback = playback
            // Only play if not playlist or an underlying media id is given (play specific song in playlist)
            if (playback !is Playlist || playback.currentPlaylistIndex != -1)
                mediaController.play()

//            User.metadata.addHistory(mediaId)
//            User.metadata.saveToLocal()
//            User.uploadMetadata()

            if (playback is Playlist) {
                val intent = Intent(this@ActivityMain, ActivityPlaylistPlayer::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@ActivityMain, ActivityPlayer::class.java)
                startActivity(intent)
            }

        }

//        binding.rvHistory.setOnItemClickListener { _, _, i, _ ->
//            playMedia(User.metadata.history[i])
//        }
//
//        User.metadata.onHistoryChanged = {
//            loadHistoryStrings()
//            (binding.rvHistory.adapter as ArrayAdapter<*>).notifyDataSetChanged()
//        }

        binding.relLayoutSongs.setOnClickListener {
            val fragment = FragmentSelectAudio(mediaController)
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
        mediaController.sendCustomAction(
            MediaAction.CombinePlaybackTypesChangedEvent,
            Bundle().apply {
                putBoolean(
                    MediaAction.CombinePlaybackTypes,
                    User.metadata.combineDifferentPlaybackTypes
                )
            })
    }

    private fun loadHistoryStrings() {
        historyStrings.clear()
//        historyStrings.addAll(User.metadata.history.map {
//            when (it) {
//                is Song -> {
//                    "*song*" + it.title + " - " + it.artist
//                }
//                is Loop -> {
//                    "*loop*" + it.name + " - " + it.song.title + " - " + it.song.artist
//                }
//                else -> it.mediaId.source
//            }
//        })
    }
}
