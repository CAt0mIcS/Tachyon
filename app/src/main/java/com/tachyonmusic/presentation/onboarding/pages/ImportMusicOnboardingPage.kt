package com.tachyonmusic.presentation.onboarding.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tachyonmusic.app.R
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.presentation.core_components.UriPermissionDialog
import com.tachyonmusic.presentation.onboarding.AnimatedButton
import com.tachyonmusic.presentation.onboarding.OnboardingPage
import com.tachyonmusic.presentation.onboarding.OnboardingViewModel
import com.tachyonmusic.presentation.profile.component.OpenDocumentDialog
import com.tachyonmusic.presentation.theme.Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class ImportMusicOnboardingPage(override val index: Int) : OnboardingPage {

    @Composable
    override fun invoke(
        viewModel: OnboardingViewModel,
        pagerState: PagerState,
        userScrollEnabledState: MutableStateFlow<Boolean>
    ) {
        val requiredMusicDirsAfterDbImport by viewModel.requiredMusicDirectoriesAfterDatabaseImport.collectAsState()
        val musicImported by viewModel.musicDirectorySelected.collectAsState()
        val readyToAdvance = musicImported && requiredMusicDirsAfterDbImport.isEmpty()
        var showUriPermissionDialog by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(musicImported) {
            if (pagerState.currentPage == index) {
                userScrollEnabledState.update { readyToAdvance }
                if (readyToAdvance)
                    pagerState.animateScrollToPage(index + 1)
            }
        }
        LaunchedEffect(requiredMusicDirsAfterDbImport) {
            if (pagerState.currentPage == index) {
                userScrollEnabledState.update { readyToAdvance }
                if (readyToAdvance)
                    pagerState.animateScrollToPage(index + 1)
            }
        }
        LaunchedEffect(pagerState.currentPage) {
            if (pagerState.currentPage == index)
                userScrollEnabledState.update { readyToAdvance }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Theme.padding.large),
                text = stringResource(R.string.import_music_onboarding_title),
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            val painter = painterResource(R.drawable.ic_equalizer)
            Image(
                modifier = Modifier
                    .weight(3f, fill = false)
                    .aspectRatio(painter.intrinsicSize.width / painter.intrinsicSize.height)
                    .fillMaxWidth(),
                painter = painter, // TODO: Generate good images for everything
                contentDescription = "Pager Image",
                contentScale = ContentScale.Fit
            )

            if (requiredMusicDirsAfterDbImport.isEmpty()) {
                var showImportDbDialog by rememberSaveable { mutableStateOf(false) }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Theme.padding.large)
                        .padding(top = Theme.padding.medium),
                    text = stringResource(R.string.import_music_onboarding_description),
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .padding(Theme.padding.medium)
                        .padding(top = Theme.padding.medium)
                        .height(IntrinsicSize.Max)
                ) {
                    AnimatedButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = Theme.padding.small)
                            .shadow(Theme.shadow.medium, shape = Theme.shapes.large),
                        visible = pagerState.currentPage == index,
                        onClick = { showUriPermissionDialog = true }) {
                        Text("Select Music Directory")
                    }

                    AnimatedButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = Theme.padding.small)
                            .shadow(Theme.shadow.medium, shape = Theme.shapes.large),
                        visible = pagerState.currentPage == index,
                        onClick = { showImportDbDialog = true }) {
                        Text("Import Database")
                    }
                }

                OpenDocumentDialog(showImportDbDialog, Database.JSON_MIME_TYPE) {
                    viewModel.onImportDatabase(it)
                    showImportDbDialog = false
                }
            } else {
                /**
                 * After choosing to import a database in the [ImportMusicOnboardingPage], we still won't have
                 * permission to access all the music directories saved in the imported database.
                 * After importing it we thus need to ask the user to select all these directories
                 */

                val requiredMusicDirsAfterDbImport by viewModel.requiredMusicDirectoriesAfterDatabaseImport.collectAsState()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Theme.padding.large)
                        .padding(vertical = Theme.padding.medium),
                    text = stringResource(R.string.import_database_onboarding_description),
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(bottom = Theme.padding.extraLarge)
                        .verticalScroll(rememberScrollState())
                ) {
                    for (requiredDir in requiredMusicDirsAfterDbImport) {
                        Row {
                            Text(
                                "\u2022",
                                modifier = Modifier
                                    .padding(bottom = Theme.padding.small)
                                    .width(Theme.padding.medium),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                requiredDir,
                                modifier = Modifier.padding(bottom = Theme.padding.small),
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }

                AnimatedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Theme.padding.medium, vertical = Theme.padding.small)
                        .shadow(Theme.shadow.medium, shape = Theme.shapes.large),
                    visible = pagerState.currentPage == index,
                    onClick = { showUriPermissionDialog = true }) {
                    Text("Select Music Directory")
                }
            }
        }

        UriPermissionDialog(showUriPermissionDialog) {
            viewModel.setNewMusicDirectory(it)
            showUriPermissionDialog = false
        }
    }
}