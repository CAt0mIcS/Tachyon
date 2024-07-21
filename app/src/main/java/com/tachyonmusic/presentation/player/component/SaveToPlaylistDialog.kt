package com.tachyonmusic.presentation.player.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tachyonmusic.presentation.player.data.PlaylistInfo
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun SaveToPlaylistDialog(
    playlists: List<PlaylistInfo>,
    onDismiss: () -> Unit,
    onCheckedChanged: (Int, Boolean) -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    var createNewPlaylist by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = Theme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth(.95f)
//                .border(1.dp, Theme.colors.orange, Theme.shapes.medium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Theme.padding.medium)
            ) {
                Text(text = "Save to...")

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(playlists.size) { i ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Checkbox(
                                checked = playlists[i].hasCurrentSong,
                                onCheckedChange = { onCheckedChanged(i, it) })

                            Text(text = playlists[i].name)
                        }
                    }
                }


                if (!createNewPlaylist) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { createNewPlaylist = true }
                    ) {
                        Text("Create New Playlist")
                    }
                } else {
                    TextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        singleLine = true
                    )
                    Button(onClick = { onCreatePlaylist(newPlaylistName) }) {
                        Text("Create")
                    }
                }
            }
        }
    }
}