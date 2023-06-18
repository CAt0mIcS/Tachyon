package com.tachyonmusic.domain.use_case.search

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import java.util.*

/**
 * Searches through all playbacks that don't come from Spotify/Soundcloud/...
 */
class SearchStoredPlaybacks(
    private val predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
    private val playbackRepository: PlaybackRepository
) {
    // TODO: Optimize
    suspend operator fun invoke(query: String?): List<Playback> {
        val playbacks =
            predefinedPlaylistsRepository.songPlaylist.value + predefinedPlaylistsRepository.customizedSongPlaylist.value + playbackRepository.getPlaylists()

        if (query.isNullOrEmpty())
            return playbacks

        // TODO: Better string searches?
        val coefficientMap = mutableMapOf<Double, Playback>()
        for (playback in playbacks) {
            var coefficient =
                diceCoefficient(if (playback is Playlist) playback.name else playback.title, query)
            while (coefficientMap.containsKey(1.0 - coefficient))
                coefficient -= .00000000001
            coefficientMap[1.0 - coefficient] = playback
        }
        return coefficientMap.toSortedMap().values.toList()
    }
}

// TODO: Move somewhere else
fun diceCoefficient(s: String?, t: String?): Double {
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