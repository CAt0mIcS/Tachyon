package com.tachyonmusic.presentation.main.component

import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tachyonmusic.presentation.main.HomeScreen
import com.tachyonmusic.presentation.main.RecommendedScreen
import com.tachyonmusic.presentation.search.PlaybackSearchScreen
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun BottomNavigation(
    navController: NavController
) {
    val items = listOf(
        HomeScreen,
        PlaybackSearchScreen,
        RecommendedScreen,
    )

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        androidx.compose.material.BottomNavigation(backgroundColor = Theme.colors.surface) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            for (item in items) {
                val selected = currentRoute == item.route

                BottomNavigationItem(
                    icon = {
                        Icon(
                            painterResource(item.icon),
                            contentDescription = stringResource(item.title),
                            modifier = Modifier.scale(1.6f) // TODO: Good practice? What happens if icons overlap?
                        )
                    },
                    selectedContentColor = Theme.colors.onSurface,
                    unselectedContentColor = Theme.colors.onBackground,
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {

                            navController.graph.startDestinationRoute?.let { screenRoute ->
                                popUpTo(screenRoute) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(0.0f,0.0f,0.0f,0.0f)
}