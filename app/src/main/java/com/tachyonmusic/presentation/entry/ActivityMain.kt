package com.tachyonmusic.presentation.entry

import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.tachyonmusic.app.R
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.domain.UriPermissionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ActivityMain : AppCompatActivity(), MediaBrowserController.EventListener {

    @Inject
    lateinit var log: Logger

    @Inject
    lateinit var mediaBrowser: MediaBrowserController

    @Inject
    lateinit var uriPermissionRepository: UriPermissionRepository

    private var castContext: CastContext? = null
    private lateinit var appUpdateManager: AppUpdateManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("TEST_EMULATOR")).build()
        )

        // Initialize the Cast context. This is required so that the media route button can be
        // created in the AppBar
        castContext = CastContext.getSharedInstance(this)

        volumeControlStream = AudioManager.STREAM_MUSIC
        mediaBrowser.registerLifecycle(lifecycle)
        mediaBrowser.registerEventListener(this)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        performUpdateCheck()
    }

    override fun onResume() {
        super.onResume()
        uriPermissionRepository.dispatchUpdate()

        // Update app if user has update downloaded but not installed
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate() // TODO: Request permission from user to restart app
                }
            }
    }


    override fun onConnected() {
        setupUi()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_activity_menu, menu)

        /**
         * Set up a MediaRouteButton to allow the user to control the current media playback route
         */
        menu?.let {
            CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
        }
        return true
    }

    private fun setupUi() {
        setContent {
            MainScreen()
        }
    }
 
    // https://developer.android.com/guide/playcore/in-app-updates/kotlin-java#kotlin
    private fun performUpdateCheck() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        val listener = InstallStateUpdatedListener {
            if(it.installStatus() == InstallStatus.DOWNLOADED)
                appUpdateManager.completeUpdate() // TODO: Request permission from user to restart app
        }

        val onUpdateResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if(it.resultCode != RESULT_OK)
                    log.warning("Update flow failed! Result ${it.resultCode}")

                appUpdateManager.unregisterListener(listener)
            }

        appUpdateManager.registerListener(listener)
        appUpdateInfoTask.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    it,
                    onUpdateResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }
    }
}
