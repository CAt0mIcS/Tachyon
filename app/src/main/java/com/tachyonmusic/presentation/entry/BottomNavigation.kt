package com.tachyonmusic.presentation.entry

import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tachyonmusic.presentation.home.HomeScreen
import com.tachyonmusic.presentation.library.LibraryScreen
import com.tachyonmusic.presentation.profile.ProfileScreen
import com.tachyonmusic.presentation.theme.NoRippleTheme
import com.tachyonmusic.presentation.theme.Theme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomNavigation(
    navController: NavController,
    sheetState: BottomSheetState
) {
    val items = listOf(
        HomeScreen,
        LibraryScreen,
        ProfileScreen,
    )

    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        androidx.compose.material.BottomNavigation(backgroundColor = Theme.colors.secondary) {
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
                    selectedContentColor = Theme.colors.contrastHigh,
                    unselectedContentColor = Theme.colors.contrastLow,
                    selected = selected,
                    onClick = {
                        if (sheetState.isExpanded) {
                            scope.launch { sheetState.collapse() }
                        }

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