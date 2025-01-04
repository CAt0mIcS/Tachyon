package com.tachyonmusic.presentation.entry

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestCompleteUpdate
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.data.model.NativeInstallAdCache
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.domain.repository.AdInterface
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.home.LoadUUIDForSongEntity
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.util.isGoogleCastAvailable
import com.tachyonmusic.playback_layers.domain.UriPermissionRepository
import com.tachyonmusic.util.ms
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@AndroidEntryPoint
class ActivityMain : AppCompatActivity(), MediaBrowserController.EventListener {

    companion object {
        const val INTENT_ACTION_SHOW_PLAYER = "com.tachyonmusic.ACTION_SHOW_PLAYER"
    }

    @Inject
    lateinit var log: Logger

    @Inject
    lateinit var mediaBrowser: MediaBrowserController

    @Inject
    lateinit var uriPermissionRepository: UriPermissionRepository

    @Inject
    lateinit var adInterface: AdInterface

    @Inject
    lateinit var loadUUIDForSongEntity: LoadUUIDForSongEntity

    @Inject
    lateinit var adCache: NativeInstallAdCache

    private var castContext: CastContext? = null
    private lateinit var appUpdateManager: AppUpdateManager

    private var updateReadyToInstall = MutableStateFlow(false)
    private var miniplayerSnapPosition = MutableStateFlow<SwipingStates?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adInterface.initialize(this)
        adCache.loadNativeInstallAds()

        // Initialize the Cast context. This is required so that the media route button can be
        // created in the AppBar
        if (isGoogleCastAvailable(this))
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
                    updateReadyToInstall.update { true }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        adCache.unloadNativeInstallAds()
        adInterface.release()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == INTENT_ACTION_SHOW_PLAYER) {
            miniplayerSnapPosition.update { SwipingStates.EXPANDED }
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
            val shouldInstallUpdate by updateReadyToInstall.collectAsState()
            val miniplayerSnapPos by miniplayerSnapPosition.collectAsState()
            MainScreen(
                shouldInstallUpdate,
                updateSnackbarResult = { shouldRestart ->
                    if (shouldRestart)
                        appUpdateManager.completeUpdate()
                    updateReadyToInstall.update { false }
                },
                miniplayerSnapPosition = miniplayerSnapPos,
                onMiniplayerSnapCompleted = { miniplayerSnapPosition.update { null } }
            )
        }
    }

    // https://developer.android.com/guide/playcore/in-app-updates/kotlin-java#kotlin
    private fun performUpdateCheck() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        val listener = InstallStateUpdatedListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED)
                updateReadyToInstall.update { true }
        }

        val onUpdateResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode != RESULT_OK)
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
