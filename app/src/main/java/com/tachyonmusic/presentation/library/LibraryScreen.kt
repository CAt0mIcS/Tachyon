package com.tachyonmusic.presentation.library

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.main.component.BottomNavigationItem
import com.tachyonmusic.presentation.theme.NoRippleTheme
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.theme.extraLarge
import kotlinx.coroutines.launch

object LibraryScreen :
    BottomNavigationItem(R.string.btmNav_library, R.drawable.ic_library, "library") {

    @Composable
    operator fun invoke(
        viewModel: LibraryViewModel = hiltViewModel()
    ) {
        var selectedFilter by remember { mutableStateOf(0) }
        var sortOptionsExpanded by remember { mutableStateOf(false) }
        var sortText by remember { mutableStateOf("Alphabetically") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Theme.padding.medium)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        Theme.shadow.small,
                        shape = Theme.shapes.extraLarge
                    ) // TODO: Shadow not working?
                    .horizontalScroll(rememberScrollState())
                    .background(Theme.colors.surface, shape = Theme.shapes.extraLarge)
                    .padding(
                        start = Theme.padding.medium,
                        top = Theme.padding.extraSmall,
                        end = Theme.padding.medium,
                        bottom = Theme.padding.extraSmall
                    ),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                FilterItem("Songs", selectedFilter == 0) {
                    selectedFilter = 0
                }

                FilterItem("Loops", selectedFilter == 1) {
                    selectedFilter = 1
                }

                FilterItem("Playlists", selectedFilter == 2) {
                    selectedFilter = 2
                }
            }

            Row(modifier = Modifier
                .padding(Theme.padding.medium)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                ) {
                    sortOptionsExpanded = true
                }
            ) {
                Box {
                    Icon(
                        painter = painterResource(R.drawable.ic_sort),
                        contentDescription = "Open Sorting Options",
                        tint = if (sortOptionsExpanded) Theme.colors.onSurface else Theme.colors.onBackground, // TODO: Animate
                        modifier = Modifier.scale(1.3f)
                    )

                    // TODO: No effect when clicking on item and dismissing the DropdownMenu
                    DropdownMenu(
                        expanded = sortOptionsExpanded,
                        onDismissRequest = { sortOptionsExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            sortText = "Alphabetically"
                            sortOptionsExpanded = false
                        }) {
                            Text("Alphabetically")
                        }

                        DropdownMenuItem(onClick = {
                            sortText = "Creation Date"
                            sortOptionsExpanded = false
                        }) {
                            Text("Creation Date")
                        }
                    }
                }

                Text(
                    modifier = Modifier.padding(start = Theme.padding.medium),
                    text = sortText,
                    fontSize = 18.sp,
                    color = if (sortOptionsExpanded) Theme.colors.onSurface else Theme.colors.onBackground // TODO: Animate
                )
            }
        }
    }
}


@Composable
private fun FilterItem(text: String, selected: Boolean = false, onClick: () -> Unit) {

    val selectedColor = Theme.colors.onSurface
    val unselectedColor = Theme.colors.primary

    val color = remember {
        Animatable(if (selected) selectedColor else unselectedColor)
    }
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            shape = Theme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (selected) Theme.colors.onSecondary else Theme.colors.primary,
                contentColor = Theme.colors.onSurface
            ),
            onClick = {
                onClick()
                scope.launch {
                    color.animateTo(
                        if (selected) unselectedColor else selectedColor,
                        animationSpec = tween(1000) // TODO: Animation not working
                    )
                }
            }
        ) {
            Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
