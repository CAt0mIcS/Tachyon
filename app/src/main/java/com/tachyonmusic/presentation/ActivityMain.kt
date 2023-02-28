package com.tachyonmusic.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.repository.UriPermissionRepository
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.domain.use_case.profile.WriteSettings
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.presentation.core_components.UriPermissionDialog
import com.tachyonmusic.presentation.player.PlayerLayout
import com.tachyonmusic.presentation.theme.ComposeSettings
import com.tachyonmusic.presentation.theme.TachyonTheme
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.hasUriPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class ActivityMain : ComponentActivity(), MediaBrowserController.EventListener {

    @Inject
    lateinit var log: Logger

    @Inject
    lateinit var mediaBrowser: MediaBrowserController

    @Inject
    lateinit var observeSettings: ObserveSettings

    @Inject
    lateinit var writeSettings: WriteSettings

    @Inject
    lateinit var uriPermissionRepository: UriPermissionRepository

    private var composeSettings = mutableStateOf(ComposeSettings())

    private val requiresMusicPathSelection = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaBrowser.registerLifecycle(lifecycle)
        mediaBrowser.registerEventListener(this)

        observeSettings().map {
            composeSettings.value = ComposeSettings(
                animateText = it.animateText
            )

            handleUriPermissions(it.musicDirectories)
        }.launchIn(lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        uriPermissionRepository.dispatchUpdate()
    }

    override fun onConnected() {
        setupUi()
    }

    private fun setNewMusicDirectory(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (hasUriPermission(uri))
                writeSettings(musicDirectories = listOf(uri))
        }
    }

    private suspend fun handleUriPermissions(musicDirs: List<Uri>) {
        val newDirs = musicDirs.filter { uri ->
            hasUriPermission(uri)
        }
        requiresMusicPathSelection.value = newDirs.isEmpty()

        if (newDirs != musicDirs)
            withContext(Dispatchers.IO) {
                writeSettings(musicDirectories = newDirs)
            }
    }


    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
    private fun setupUi() {
        setContent {
            TachyonTheme(settings = composeSettings.value) {

                UriPermissionDialog(requiresMusicPathSelection.value) {
                    if (it != null) {
                        contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        setNewMusicDirectory(it)
                    }
                }

                val sheetState = rememberBottomSheetState(
                    initialValue = BottomSheetValue.Collapsed, animationSpec = tween(
                        durationMillis = Theme.animation.medium, easing = LinearEasing
                    )
                )
                val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

                val miniPlayerHeight = remember { mutableStateOf(0.dp) }
                val navController = rememberAnimatedNavController()

                Scaffold(bottomBar = {
                    BottomNavigation(navController, sheetState)
                }) { innerPaddingScaffold ->

                    BottomSheetScaffold(
                        modifier = Modifier.padding(innerPaddingScaffold),
                        scaffoldState = scaffoldState,
                        sheetContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                            ) {
                                if (!requiresMusicPathSelection.value)
                                    PlayerLayout(
                                        sheetState,
                                        onMiniPlayerHeight = { miniPlayerHeight.value = it },
                                        miniPlayerHeight = miniPlayerHeight.value
                                    )
                            }
                        },
                        sheetPeekHeight = miniPlayerHeight.value,
                        sheetBackgroundColor = Theme.colors.primary
                    ) { innerPaddingSheet ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPaddingSheet)
                        ) {
                            if (!requiresMusicPathSelection.value)
                                NavigationGraph(navController, sheetState, miniPlayerHeight)
                        }
                    }
                }
            }
        }
    }
}
