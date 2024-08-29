package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.util.removeFirst
import java.util.Arrays

/**
 * If [diceCoefficient] returns a value >= than [DICE_COEFFICIENT_GUARANTEED_RESULT_THRESHOLD] then
 * the playback is seen as a guaranteed hit and will be added to the range of counted returns
 */
private const val DICE_COEFFICIENT_GUARANTEED_RESULT_THRESHOLD = .55f

data class PlaybackSearchResult(
    val playback: Playback? = null,
    val playlist: Playlist? = null,
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
        playbackType: PlaybackType,
        range: Int = Int.MAX_VALUE
    ): List<PlaybackSearchResult> {
        return when (playbackType) {
            is PlaybackType.Song -> searchSongs(query, range)
            is PlaybackType.Remix -> searchRemixes(query, range)
            is PlaybackType.Playlist -> searchPlaylists(query, range)
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


    private suspend fun searchSongs(
        query: String,
        range: Int = Int.MAX_VALUE
    ): List<PlaybackSearchResult> {
        if (query.isBlank()) return emptyList()

        var songs = playbackRepository.getSongs()
        var guaranteedResults = 0.0f

        val results = mutableListOf<PlaybackSearchResult>()

        songs = songs.filter { song ->
            val result = if (song.title.containsEqual(query)) {
                guaranteedResults += 1
                PlaybackSearchResult(song, null, findHighlights(query, song.title), score = 1f)
            } else if (song.artist.containsEqual(query)) {
                guaranteedResults += .9f
                PlaybackSearchResult(
                    song,
                    artistHighlightIndices = findHighlights(query, song.artist),
                    score = .99f
                )
            } else if (song.album?.containsEqual(query) == true) {
                guaranteedResults += .9f
                PlaybackSearchResult(
                    song,
                    albumHighlightIndices = findHighlights(query, song.album ?: ""),
                    score = .9f
                )
            } else null

            results.add(result ?: return@filter true)
            false
        }

        for (song in songs) {
            if (guaranteedResults >= range) {
                break
            }

            val score = computeScore(query, "${song.title} ${song.artist} ${song.album ?: ""}")
            if (score >= DICE_COEFFICIENT_GUARANTEED_RESULT_THRESHOLD)
                guaranteedResults += .8f

            results.add(PlaybackSearchResult(song, score = score))
        }

        return results.sortedByDescending { it.score }.filter { it.score != 0f }
    }

    private suspend fun searchRemixes(
        query: String,
        range: Int = Int.MAX_VALUE
    ): List<PlaybackSearchResult> {
        if (query.isBlank()) return emptyList()

        var remixes = playbackRepository.getRemixes()
        var guaranteedResults = 0.0f
        val results = mutableListOf<PlaybackSearchResult>()

        remixes = remixes.filter { remix ->
            val result = if (remix.name!!.containsEqual(query)) {
                guaranteedResults += 1f
                PlaybackSearchResult(
                    remix,
                    nameHighlightIndices = findHighlights(query, remix.name!!),
                    score = 1f
                )
            } else if (remix.title.containsEqual(query)) {
                guaranteedResults += 1f
                PlaybackSearchResult(remix, null, findHighlights(query, remix.title), score = 1f)
            } else if (remix.artist.containsEqual(query)) {
                guaranteedResults += .9f
                PlaybackSearchResult(
                    remix,
                    artistHighlightIndices = findHighlights(query, remix.artist),
                    score = .99f
                )
            } else if (remix.album?.containsEqual(query) == true) {
                guaranteedResults += .9f
                PlaybackSearchResult(
                    remix,
                    albumHighlightIndices = findHighlights(query, remix.album ?: ""),
                    score = .9f
                )
            } else
                null

            results.add(result ?: return@filter true)
            false
        }

        for (remix in remixes) {
            if (guaranteedResults >= range) {
                break
            }

            val score = computeScore(
                query,
                "${remix.name} ${remix.title} ${remix.artist} ${remix.album ?: ""}"
            )
            if (score >= DICE_COEFFICIENT_GUARANTEED_RESULT_THRESHOLD)
                guaranteedResults += .8f

            results.add(PlaybackSearchResult(remix, score = score))
        }
        return results.sortedByDescending { it.score }.filter { it.score != 0f }
    }

    private suspend fun searchPlaylists(
        query: String,
        range: Int = Int.MAX_VALUE
    ): List<PlaybackSearchResult> {
        if (query.isBlank()) return emptyList()

        var playlists = playbackRepository.getPlaylists()
        var guaranteedResults = 0.0f
        val results = mutableListOf<PlaybackSearchResult>()

        playlists = playlists.filter { playlist ->
            val result = if (playlist.name.containsEqual(query)) {
                guaranteedResults += 1f
                PlaybackSearchResult(
                    null,
                    playlist,
                    nameHighlightIndices = findHighlights(query, playlist.name),
                    score = 1f
                )
            } else
                PlaybackSearchResult(null, playlist)

            // TODO: Do the same for album, artist, name, ...?
            val titles = playlist.playbacks.map { it.title }
            for (title in titles) {
                if (title.containsEqual(query)) {
                    result.score += 1f / titles.size
                    guaranteedResults += result.score
                }
            }

            results.add(if (result.score >= .1f) result else return@filter true)
            false
        }

        for (playlist in playlists) {
            if (guaranteedResults >= range) {
                break
            }

            val result = PlaybackSearchResult(null, playlist)
            result.score = computeScore(query, playlist.name)

            if (result.score >= DICE_COEFFICIENT_GUARANTEED_RESULT_THRESHOLD)
                guaranteedResults += 1f

            results.add(result)
        }

        return results.sortedByDescending { it.score }.filter { it.score != 0f }
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