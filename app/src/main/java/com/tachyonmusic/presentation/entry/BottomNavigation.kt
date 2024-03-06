package com.tachyonmusic.presentation.entry

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SheetState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigation(
    navController: NavController,
    sheetState: SheetState,
    onSheetStateFraction: (Float) -> Unit
) {
    val items = listOf(
        HomeScreen,
        LibraryScreen,
        ProfileScreen,
    )

    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        androidx.compose.material3.NavigationBar(containerColor = Theme.colors.secondary) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            for (item in items) {
                val selected = currentRoute == item.route

                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(item.icon),
                            contentDescription = stringResource(item.title),
                            modifier = Modifier.scale(1.6f) // TODO: Good practice? What happens if icons overlap?
                        )
                    },
                    colors = NavigationBarItemDefaults.colors().copy(selectedIconColor = Theme.colors.contrastHigh, unselectedIconColor = Theme.colors.contrastLow),
//                    selectedContentColor = Theme.colors.contrastHigh, // TODO MAT3 ^
//                    unselectedContentColor = Theme.colors.contrastLow, // TODO MAT3 ^
                    selected = selected,
                    onClick = {
                        if (sheetState.hasExpandedState) {
                            scope.launch {
                                sheetState.partialExpand()
                            }
                            onSheetStateFraction(0f)
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