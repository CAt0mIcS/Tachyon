package com.tachyonmusic.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.presentation.onboarding.pages.FirstOnboardingPage
import com.tachyonmusic.presentation.onboarding.pages.ImportMusicOnboardingPage
import com.tachyonmusic.presentation.onboarding.pages.LastOnboardingPage
import com.tachyonmusic.presentation.theme.interpolate
import kotlinx.coroutines.flow.MutableStateFlow
import mx.platacard.pagerindicator.PagerIndicatorOrientation
import mx.platacard.pagerindicator.PagerWormIndicator

interface OnboardingPage {
    val index: Int

    @Composable
    operator fun invoke(
        viewModel: OnboardingViewModel,
        pagerState: PagerState,
        userScrollEnabledState: MutableStateFlow<Boolean>
    )
}

object OnboardingScreen {

    @Composable
    operator fun invoke(
        viewModel: OnboardingViewModel = hiltViewModel()
    ) {
        val pages = listOf<OnboardingPage>(
            FirstOnboardingPage(0),
            ImportMusicOnboardingPage(1),
            LastOnboardingPage(2)
        )

        val userScrollEnabledState = remember { MutableStateFlow(true) }
        val userScrollEnabled by userScrollEnabledState.collectAsState()

        val pagerState = rememberPagerState { pages.size }

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                modifier = Modifier.weight(10f),
                state = pagerState,
                verticalAlignment = Alignment.Top,
                userScrollEnabled = userScrollEnabled
            ) { position ->
                pages[position](viewModel, pagerState, userScrollEnabledState)
            }
            PagerWormIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .weight(1f),
                pagerState = pagerState,
                activeDotColor = MaterialTheme.colorScheme.primary,
                dotColor = MaterialTheme.colorScheme.primary.interpolate(MaterialTheme.colorScheme.onPrimaryContainer)
                    .interpolate(MaterialTheme.colorScheme.background),
                dotCount = pages.size,
                orientation = PagerIndicatorOrientation.Horizontal
            )

            AnimatedButton(
                modifier = Modifier.weight(1f),
                pagerState.currentPage == pages.last().index,
                onClick = {
                    viewModel.saveOnboardingState(true)
                }) {
                Text("Finish Setup")
            }
        }
    }
}


@Composable
fun AnimatedButton(
    modifier: Modifier,
    visible: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .padding(horizontal = 40.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = visible
        ) {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White
                ),
                content = content
            )
        }
    }
}