package com.tachyonmusic.media.domain.use_case

import com.google.common.collect.ImmutableList


fun <T : Any> getItemsOnPageWithPageSize(items: List<T>, page: Int, pageSize: Int): ImmutableList<T> {
    val maxIdx = page * pageSize + pageSize - 1
    val range =
        if (maxIdx > items.size - 1) (page * pageSize until items.size)
        else (page * pageSize until maxIdx + 1)

    return ImmutableList.Builder<T>().apply {
        for (i in range)
            add(items[i])
    }.build()
}
