package com.daton.mucify.ui

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.widget.SeekBar
import com.daton.media.MediaAction
import com.daton.media.device.BrowserTree
import com.daton.mucify.R
import com.daton.mucify.permission.Permission
import com.daton.mucify.permission.PermissionManager
import com.daton.mucify.user.User
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ActivityMain : MediaControllerActivity() {
    companion object {
        const val TAG = "ActivityMain"
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var hasStoragePermission: Boolean = false

    /**
     * Counts down when the storage permission was either accepted or denied
     */
    private var permissionResultAvailable = CountDownLatch(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        findViewById<SeekBar>(R.id.sbPos).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                User.login(this@ActivityMain)
            }
        })

        findViewById<SeekBar>(R.id.sbStartPos).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                User.logout(this@ActivityMain)
            }
        })
    }


    override fun onConnected() {
        mediaBrowser.subscribe(
            BrowserTree.ROOT,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    // Play only if not currently playing
                    if (!isCreated) {
                        mediaId = children[0].mediaId.toString()
                        play()
                    }
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
}