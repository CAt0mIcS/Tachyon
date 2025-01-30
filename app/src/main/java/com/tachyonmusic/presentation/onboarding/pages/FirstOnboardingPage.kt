package com.tachyonmusic.presentation.onboarding.pages

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.tachyonmusic.presentation.onboarding.OnboardingPage
import com.tachyonmusic.presentation.onboarding.OnboardingViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class FirstOnboardingPage(override val index: Int) : OnboardingPage {
    @Composable
    override fun invoke(
        viewModel: OnboardingViewModel,
        pagerState: PagerState,
        userScrollEnabledState: MutableStateFlow<Boolean>
    ) {
        Text("Hello World")
    }
}