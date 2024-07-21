package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import java.util.Arrays

data class PlaybackSearchResult(
    val playback: Playback,
    val titleHighlightIndices: List<Int> = emptyList(),
    val artistHighlightIndices: List<Int> = emptyList(),
    val albumHighlightIndices: List<Int> = emptyList(),
    val nameHighlightIndices: List<Int> = emptyList(),
    var score: Float = 0f,
)

/**
 * Searches through all playbacks that don't come from Spotify/Soundcloud/...
 */
class SearchStoredPlaybacks(
    private val playbackRepository: PlaybackRepository
) {
    suspend operator fun invoke(
        query: String,
        playbackType: PlaybackType
    ): List<PlaybackSearchResult> {
        return when (playbackType) {
            is PlaybackType.Song -> searchSongs(query)
            is PlaybackType.Remix -> searchRemixes(query)
            is PlaybackType.Playlist -> searchPlaylists(query)
            is PlaybackType.Ad -> {
                emptyList()
            }
        }
    }

    suspend fun byPlaylist(name: String?): List<PlaybackSearchResult> {
        if (name == null)
            return emptyList()
        return searchPlaylists(name)
    }

    suspend fun byTitleArtist(title: String?, artist: String?): List<PlaybackSearchResult> {
        if (title == null && artist == null)
            return emptyList()

        return (searchSongs("$title $artist") + searchRemixes("$title $artist")).sortedByDescending { it.score }
    }


    private suspend fun searchSongs(query: String): List<PlaybackSearchResult> {
        if (query.isBlank()) return emptyList()

        val songs = playbackRepository.getSongs()

        return List(songs.size) { i ->
            val song = songs[i]

            if (song.title.containsEqual(query))
                PlaybackSearchResult(song, findHighlights(query, song.title), score = 1f)
            else if (song.artist.containsEqual(query))
                PlaybackSearchResult(
                    song,
                    artistHighlightIndices = findHighlights(query, song.artist),
                    score = .99f
                )
            else if (song.album?.containsEqual(query) == true)
                PlaybackSearchResult(
                    song,
                    albumHighlightIndices = findHighlights(query, song.album ?: ""),
                    score = .9f
                )
            else
                PlaybackSearchResult(
                    song,
                    score = computeScore(query, "${song.title} ${song.artist} ${song.album ?: ""}")
                )
        }.sortedByDescending { it.score }.filter { it.score != 0f }
    }

    private suspend fun searchRemixes(query: String): List<PlaybackSearchResult> {
        if (query.isBlank()) return emptyList()

        val remixes = playbackRepository.getRemixes()

        return List(remixes.size) { i ->
            val remix = remixes[i]

            if (remix.name.containsEqual(query))
                PlaybackSearchResult(
                    remix,
                    nameHighlightIndices = findHighlights(query, remix.name),
                    score = 1f
                )
            else if (remix.title.containsEqual(query))
                PlaybackSearchResult(remix, findHighlights(query, remix.title), score = 1f)
            else if (remix.artist.containsEqual(query))
                PlaybackSearchResult(
                    remix,
                    artistHighlightIndices = findHighlights(query, remix.artist),
                    score = .99f
                )
            else if (remix.album?.containsEqual(query) == true)
                PlaybackSearchResult(
                    remix,
                    albumHighlightIndices = findHighlights(query, remix.album ?: ""),
                    score = .9f
                )
            else
                PlaybackSearchResult(
                    remix, score = computeScore(
                        query,
                        "${remix.name} ${remix.title} ${remix.artist} ${remix.album ?: ""}"
                    )
                )
        }.sortedByDescending { it.score }.filter { it.score != 0f }
    }

    private suspend fun searchPlaylists(query: String): List<PlaybackSearchResult> {
        if (query.isBlank()) return emptyList()

        val playlists = playbackRepository.getPlaylists()

        return List(playlists.size) { i ->
            val playlist = playlists[i]

            var result: PlaybackSearchResult?

            if (playlist.name.containsEqual(query))
                result = PlaybackSearchResult(
                    playlist,
                    nameHighlightIndices = findHighlights(query, playlist.name),
                    score = 1f
                )

            result = PlaybackSearchResult(playlist)
            // TODO: Do the same for album, artist, name, ...?
            val titles = playlist.playbacks.map { it.title }
            for (title in titles) {
                if (title.containsEqual(query)) {
                    result.score += 1f / titles.size
                }
            }

            if (result.score <= .1f)
                result.score = computeScore(query, playlist.name)
            result
        }.sortedByDescending { it.score }.filter { it.score != 0f }
    }


    // TODO: Take portions into account, so that e.g. Ashanaha doesn't highlight all letters 'a' and only the part that is searched for
    private fun findHighlights(query: String, evaluate: String): List<Int> {
        val highlights = mutableListOf<Int>()

        evaluate.forEachIndexed { index, letter ->
            if (query.contains(letter))
                highlights.add(index)
        }

        return highlights
    }
}

// TODO: needs to be better
private fun computeScore(query: String, evaluate: String): Float {
    return diceCoefficient(evaluate, query).toFloat()
}


private fun diceCoefficient(s: String?, t: String?): Double {
    // Verifying the input:
    if (s == null || t == null) return 0.0
    // Quick check to catch identical objects:
    if (s === t) return 1.0
    // avoid exception for single character searches
    if (s.length < 2 || t.length < 2) return 0.0

    // Create the bigrams for string s:
    val n = s.length - 1
    val sPairs = IntArray(n)
    for (i in 0..n) if (i == 0) sPairs[i] = s[i].code shl 16 else if (i == n) sPairs[i - 1] =
        sPairs[i - 1] or s[i].code else sPairs[i] = s[i].let {
        sPairs[i - 1] = sPairs[i - 1] or it.toInt(); sPairs[i - 1]
    } shl 16

    // Create the bigrams for string t:
    val m = t.length - 1
    val tPairs = IntArray(m)
    for (i in 0..m) if (i == 0) tPairs[i] = t[i].code shl 16 else if (i == m) tPairs[i - 1] =
        tPairs[i - 1] or t[i].code else tPairs[i] = t[i].let {
        tPairs[i - 1] = tPairs[i - 1] or it.toInt(); tPairs[i - 1]
    } shl 16

    // Sort the bigram lists:
    Arrays.sort(sPairs)
    Arrays.sort(tPairs)

    // Count the matches:
    var matches = 0
    var i = 0
    var j = 0
    while (i < n && j < m) {
        if (sPairs[i] == tPairs[j]) {
            matches += 2
            i++
            j++
        } else if (sPairs[i] < tPairs[j]) i++ else j++
    }
    return matches.toDouble() / (n + m)
}

private fun String.containsEqual(other: String) =
    contains(other, ignoreCase = true) || other.contains(this, ignoreCase = true)