package com.tachyonmusic.media.data

import com.tachyonmusic.media.domain.model.TestSong
import org.junit.Before


class BrowserTreeTest {

    private lateinit var repository: TestUserRepository
    private lateinit var browserTree: BrowserTree

    @Before
    fun setUp() {
        repository = TestUserRepository()
        browserTree = BrowserTree(repository)

        repository.complete(

        )
    }
}