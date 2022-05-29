package com.daton.mucify.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.widget.ArrayAdapter
import com.daton.media.MediaAction
import com.daton.media.device.BrowserTree
import com.daton.media.device.Loop
import com.daton.media.ext.endTime
import com.daton.media.ext.isLoop
import com.daton.media.ext.path
import com.daton.media.ext.startTime
import com.daton.mucify.R
import com.daton.mucify.databinding.ActivityMainBinding
import com.daton.mucify.permission.Permission
import com.daton.mucify.permission.PermissionManager
import com.daton.mucify.user.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch


class ActivityMain : MediaControllerActivity() {
    companion object {
        const val TAG = "ActivityMain"
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var hasStoragePermission: Boolean = false

    private lateinit var binding: ActivityMainBinding

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

        User.create(this)

        Log.d(TAG, "onCreate finished")
    }


    override fun onConnected() {
        mediaBrowser.subscribe(
            BrowserTree.ROOT,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    User.onLogin {
                        // TODO: Subscribe to [BrowserTree.LOOP_ROOT] so that the filter is not necessary
                        for (child in children.filter { it.isLoop }) {
                            val localLoop =
                                Loop(
                                    child.mediaId!!,
                                    child.path.absolutePath,
                                    child.startTime,
                                    child.endTime
                                )

                            // Check if media id of local loop is in User.metadata.loops (remote)
                            for (loop in User.metadata.loops) {

                                // Exact loop already saved on device, no action necessary
                                if (loop == localLoop)
                                    continue

                                // Some part of the loop is not the same, decide which to keep
                                if (loop.mediaId == localLoop.mediaId) {
                                    TODO("Merge loops and decide which to keep")
                                }

                                // Loop is only stored offline
                            }

                            // Media id of local loop NOT contained in User.metadata.loops (remote)
                            // Add loop to metadata
                            User.metadata += localLoop
                        }


                        val loopsToSend = mutableListOf<Loop>()
                        for (remoteLoop in User.metadata.loops) {
                            // Check if media id of local loop is in User.metadata.loops (remote)
                            for (child in children.filter { it.isLoop }) {
                                val localLoop =
                                    Loop(
                                        child.mediaId!!,
                                        child.path.absolutePath,
                                        child.startTime,
                                        child.endTime
                                    )

                                // Exact loop already saved on device, no action necessary
                                if (remoteLoop == localLoop)
                                    continue

                                // Some part of the loop is not the same, decide which to keep
                                if (remoteLoop.mediaId == localLoop.mediaId) {
                                    TODO("Merge loops and decide which to keep")
                                }

                                // Loop is only stored online
                            }

                            // Media id of local loop NOT contained in User.metadata.loops (remote)
                            // Add loop to metadata
                            loopsToSend += remoteLoop
                        }
                    }

                    // TODO: Decide when to upload (in onPause? | every time we change sth? | ...)
                    User.uploadMetadata()

                    setupUI(children)
                }
            })

        serviceScope.launch {
            // Wait for permission dialog to be accepted or denied
            permissionResultAvailable.await()

            // Notify service to load local device files
            if (hasStoragePermission) {
                val bundle = Bundle()
                bundle.putBoolean(MediaAction.StoragePermissionGranted, true)
                sendCustomAction(MediaAction.StoragePermissionChanged, bundle)
            }
        }
    }

    private fun setupUI(playbacks: List<MediaBrowserCompat.MediaItem>) {
        Log.d(TAG, "Setting up ui")

        binding.btnLogin.setOnClickListener { User.login(this) }
        binding.btnLogout.setOnClickListener { User.logout(this) }

        binding.rvHistory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            User.metadata.history
        )

        val playMedia = { mediaId: String ->
            this.mediaId = mediaId
            play()

            User.metadata.addHistory(mediaId)
            User.metadata.saveToLocal()
            User.uploadMetadata()
        }

        binding.rvHistory.setOnItemClickListener { adapterView, view, i, l ->
            playMedia(adapterView.getItemAtPosition(i).toString())
        }

        User.metadata.onHistoryChanged = {
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
}
