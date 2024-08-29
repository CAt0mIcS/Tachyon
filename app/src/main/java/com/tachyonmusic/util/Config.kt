package com.tachyonmusic.util

object Config {
    /**
     * Amount by which the maximum number of stored remixes is increased after watching an ad
     */
    const val MAX_REMIX_INCREMENT_AMOUNT = 10

    const val SEARCH_ARTWORK_LOAD_QUALITY = 50

    /**
     * When seeking to the end of a timing data interval using the buttons we want to seek
     * a couple seconds before the end to allow for better adjustments. This value controls how
     * many seconds before the end of a timing data we seek
     */
    val TIMING_DATA_END_TIME_ADJUSTMENT = 3.sec
}