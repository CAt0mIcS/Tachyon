package com.tachyonmusic.user.data.repository

import android.os.Environment
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tachyonmusic.core.Resource
import com.tachyonmusic.core.UiText
import com.tachyonmusic.core.domain.model.*
import com.tachyonmusic.user.R
import com.tachyonmusic.user.data.Metadata
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.launch
import kotlinx.coroutines.*
import java.io.File

class FirebaseRepository : UserRepository {

    override val songs: Deferred<List<Song>>
        get() = _songs
    override val loops: Deferred<List<Loop>>
        get() = metadata.loops
    override val playlists: Deferred<List<Playlist>>
        get() = metadata.playlists

    private val _songs = CompletableDeferred<ArrayList<Song>>()

    private var metadata: Metadata = Metadata()

    override val signedIn: Boolean
        get() = Firebase.auth.currentUser != null

    private var eventListener: UserRepository.EventListener? = null

    init {
        val files =
            File(Environment.getExternalStorageDirectory().absolutePath + "/Music/").listFiles()!!
        Log.d("FirebaseRepository", "Started loading songs")
        val songs = arrayListOf<Song>()
        for (file in files) {
            if (file.extension == "mp3") {
                songs += Song(file)
            }
        }
        Log.d("FirebaseRepository", "Finished loading songs")
        _songs.complete(songs)
    }

    override suspend fun signIn(
        email: String,
        password: String
    ) = withContext(Dispatchers.IO) {
        val job = CompletableDeferred<Resource<Unit>>()

        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    launch(Dispatchers.IO) {
                        initialize()
                        job.complete(Resource.Success())
                    }
                } else
                    job.complete(
                        Resource.Error(
                            if (it.exception?.localizedMessage != null)
                                UiText.DynamicString(it.exception!!.localizedMessage!!)
                            else UiText.StringResource(R.string.unknown_error)
                        )
                    )
            }
        return@withContext job.await()
    }

    override suspend fun register(
        email: String,
        password: String
    ) = withContext(Dispatchers.IO) {
        val job = CompletableDeferred<Resource<Unit>>()
        job.start()

        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    launch(Dispatchers.IO) {
                        initialize()
                        job.complete(Resource.Success())
                    }
                } else
                    job.complete(
                        Resource.Error(
                            if (it.exception?.localizedMessage != null)
                                UiText.DynamicString(it.exception!!.localizedMessage!!)
                            else UiText.StringResource(R.string.unknown_error)
                        )
                    )
            }
        return@withContext job.await()
    }

    override fun signOut() = Firebase.auth.signOut()

    // TODO: Use [Firebase.firestore.update] (https://firebase.google.com/docs/firestore/manage-data/add-data#update-data) && (https://firebase.google.com/docs/firestore/manage-data/add-data#update_elements_in_an_array)

    override fun upload() {
        // TODO: When not signed in and history changes, signing in will only download
        // TODO: history from firebase and not keep local changes. Should be fixed once we figure
        // TODO: out how to remove this check and query the upload if the user is not signed in

        if (signedIn) {
            Firebase.firestore.collection("users")
                .document(Firebase.auth.currentUser!!.uid)
                .set(metadata.toHashMap())
        }
        // TODO: If user never signed in before: Store locally
        // TODO: If user currently not signed in: Tell firebase to upload once online and signed in
    }

    override fun registerEventListener(listener: UserRepository.EventListener?) {
        eventListener = listener
    }

    private suspend fun initialize() {
        val job = Job()

        if (signedIn) {
            Firebase.firestore.collection("users")
                .document(Firebase.auth.currentUser!!.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.result.data != null)
                        launch(Dispatchers.IO) {
                            metadata = Metadata(task.result.data!!)
                            job.complete()
                        }
                }

        } else {
            Firebase.firestore.disableNetwork().addOnCompleteListener {
                Firebase.firestore.collection("users")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.result.documents.isNotEmpty() && task.result.documents[0].data != null)
                            launch(Dispatchers.IO) {
                                metadata = Metadata(task.result.documents[0].data!!)
                                job.complete()
                            }
                    }
                Firebase.firestore.enableNetwork()
            }
        }

        job.join()
    }

    suspend operator fun plusAssign(song: Song) {
        _songs.await().add(song)
        _songs.await().sortBy { it.title + it.artist }
        eventListener?.onSongListChanged(song)
    }

    suspend operator fun plusAssign(loop: Loop) {
        metadata.loops.await().add(loop)
        eventListener?.onLoopListChanged(loop)
    }

    suspend operator fun plusAssign(playlist: Playlist) {
        metadata.playlists.await().add(playlist)
        eventListener?.onPlaylistListChanged(playlist)
    }

    override suspend fun find(mediaId: MediaId): Playback? {
        val s = songs.await().find { it.mediaId == mediaId }
        if (s != null)
            return s
        val l = loops.await().find { it.mediaId == mediaId }
        if (l != null)
            return l
        return playlists.await().find { it.mediaId == mediaId }
    }
}