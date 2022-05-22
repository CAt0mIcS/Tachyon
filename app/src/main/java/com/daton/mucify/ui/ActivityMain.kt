package com.daton.mucify.ui

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.daton.media.MediaAction
import com.daton.media.device.BrowserTree
import com.daton.mucify.R
import com.daton.mucify.UserSettings
import com.daton.mucify.permission.Permission
import com.daton.mucify.permission.PermissionManager
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
     * Auto0
     */
    private lateinit var account: Auth0
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

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

//        account = Auth0(
//            getString(R.string.auth0_client_id),
//            getString(R.string.com_auth0_domain)
//        )

        UserSettings.load(this)
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
//                        play()
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

    private fun login() {
        WebAuthProvider
            .login(account)
            .withScheme(getString(R.string.com_auth0_scheme))
            .withScope(getString(R.string.auth0_login_scopes))
            .withAudience(
                getString(
                    R.string.auth0_login_audience,
                    getString(R.string.com_auth0_domain)
                )
            )
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Failed to authenticate with auth0 ${error.statusCode}")
                }

                override fun onSuccess(result: Credentials) {
                    cachedCredentials = result
                }

            })
    }

    private fun logout() {
        WebAuthProvider
            .logout(account)
            .withScheme(getString(R.string.com_auth0_scheme))
            .start(this, object : Callback<Void?, AuthenticationException> {

                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Failed to log out ${error.statusCode}")
                }

                override fun onSuccess(result: Void?) {
                    cachedCredentials = null
                    cachedUserProfile = null
                }
            })
    }
}