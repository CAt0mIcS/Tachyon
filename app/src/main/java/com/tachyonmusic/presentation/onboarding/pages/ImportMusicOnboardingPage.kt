package com.tachyonmusic.presentation.onboarding.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                userScrollEnabledState.update { musicImported }
        }

        if (requiredMusicDirsAfterDbImport.isEmpty()) {
            var showUriPermissionDialog by remember { mutableStateOf(false) }
            var showImportDbDialog by remember { mutableStateOf(false) }


            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight(0.7f),
                    painter = painterResource(R.drawable.ic_equalizer), // TODO: Generate good images for everything
                    contentDescription = "Pager Image"
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.import_music_onboarding_title),
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .padding(top = 20.dp),
                    text = stringResource(R.string.import_music_onboarding_description),
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Row(modifier = Modifier.weight(1f).padding(horizontal = Theme.padding.large)) {
                    AnimatedButton(
                        modifier = Modifier.weight(1f),
                        visible = pagerState.currentPage == index,
                        onClick = { showUriPermissionDialog = true }) {
                        Text("Select Music Directory")
                    }

                    AnimatedButton(
                        modifier = Modifier.weight(1f),
                        visible = pagerState.currentPage == index,
                        onClick = { showImportDbDialog = true }) {
                        Text("Import Database")
                    }
                }
            }

            UriPermissionDialog(showUriPermissionDialog) {
                viewModel.setNewMusicDirectory(it)
                showUriPermissionDialog = false
            }

            OpenDocumentDialog(showImportDbDialog, Database.JSON_MIME_TYPE) {
                viewModel.onImportDatabase(it)
                showImportDbDialog = false
            }
        } else {
            SelectMusicDirectoriesForDatabaseImportPage(index)(
                viewModel,
                pagerState,
                userScrollEnabledState
            )
        }
    }
}