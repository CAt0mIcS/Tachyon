package com.tachyonmusic.domain.use_case.search

import com.tachyonmusic.app.R
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.flow
import java.util.*

/**
 * Searches through all playbacks that don't come from Spotify/Soundcloud/...
 */
class SearchStoredPlaybacks(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(query: String?) = flow {
        if (query.isNullOrEmpty()) {
            emit(
                Resource.Error(
                    UiText.StringResource(
                        R.string.invalid_search_query,
                        "null/empty"
                    )
                )
            )
            return@flow
        }

        // TODO: Optimize
        val playbacks =
            songRepository.getSongs() + loopRepository.getLoops() + playlistRepository.getPlaylists()

        // TODO: Better string searches?
        for (playback in playbacks) {
            val coefficient = diceCoefficient(playback.title, query)
            emit(Resource.Success(playback to 1f - coefficient))
        }
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