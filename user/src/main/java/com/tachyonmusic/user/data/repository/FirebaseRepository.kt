package com.tachyonmusic.user.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.user.R
import com.tachyonmusic.user.data.LocalCache
import com.tachyonmusic.user.data.Metadata
import com.tachyonmusic.user.domain.FileRepository
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import com.tachyonmusic.util.launch
import kotlinx.coroutines.*

class FirebaseRepository(
    private var fileRepository: FileRepository,
    val localCache: LocalCache,
    private val gson: Gson,
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore
) : UserRepository, IListenable<UserRepository.EventListener> by Listenable() {

    override val songs: Deferred<List<Song>>
        get() = fileRepository.songs
    override val loops: Deferred<List<Loop>>
        get() = metadata.loops
    override val playlists: Deferred<List<Playlist>>
        get() = metadata.playlists

    private var metadata: Metadata = if (localCache.exists) localCache.get() else Metadata(gson)

    override val signedIn: Boolean
        get() = auth.currentUser != null

    init {
        registerEventListener(localCache)
    }

    override suspend fun signIn(
        email: String,
        password: String
    ) = withContext(Dispatchers.IO) {
        val job = CompletableDeferred<Resource<Unit>>()

        auth.signInWithEmailAndPassword(email, password)
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

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    launch(Dispatchers.IO) {
                        upload()
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

    override fun signOut() {
        auth.signOut()
        metadata = Metadata(gson)
        invokeEvent {
            it.onUserChanged(null)
        }
    }

    override suspend fun delete() = withContext(Dispatchers.IO) {
        val job = CompletableDeferred<Resource<Unit>>()

        if (auth.currentUser != null)
            firestore.collection("users")
                .document(auth.currentUser!!.uid).delete()

        auth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                metadata = Metadata(gson)
                invokeEvent {
                    it.onUserChanged(null)
                }
                job.complete(Resource.Success())
            } else
                job.complete(
                    Resource.Error(
                        if (task.exception?.localizedMessage != null)
                            UiText.DynamicString(task.exception!!.localizedMessage!!)
                        else UiText.StringResource(R.string.unknown_error)
                    )
                )
        }

        job.await()
    }

    // TODO: Use [Firebase.firestore.update] (https://firebase.google.com/docs/Firebase.firestore/manage-data/add-data#update-data) && (https://firebase.google.com/docs/Firebase.firestore/manage-data/add-data#update_elements_in_an_array)

    override suspend fun upload() = withContext(Dispatchers.IO) {
        // TODO: When not signed in and history changes, signing in will only download
        // TODO: history from firebase and not keep local changes. Should be fixed once we figure
        // TODO: out how to remove this check and query the upload if the user is not signed in

        val job = CompletableDeferred<Resource<Unit>>()

        if (signedIn) {
            firestore.collection("users")
                .document(auth.currentUser!!.uid)
                .set(metadata.toHashMap())
                .addOnCompleteListener {
                    job.complete(
                        if (it.isSuccessful) Resource.Success()
                        else Resource.Error(
                            if (it.exception?.localizedMessage != null) UiText.DynamicString(it.exception!!.localizedMessage!!)
                            else UiText.StringResource(R.string.unknown_error)
                        )
                    )
                }
        } else
            job.complete(localCache.set(metadata))
        // TODO: If user never signed in before: Store locally
        // TODO: If user currently not signed in: Tell firebase to upload once online and signed in
        job.await()
    }

    private suspend fun initialize() {
        val job = Job()

        if (signedIn) {
            firestore.collection("users")
                .document(auth.currentUser!!.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.result.data != null)
                        launch(Dispatchers.IO) {
                            metadata = Metadata(gson, task.result.data!!)
                            job.complete()
                        }
                    else
                        job.complete()
                }

        } else {
            firestore.disableNetwork().addOnCompleteListener {
                firestore.collection("users")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.result.documents.isNotEmpty() && task.result.documents[0].data != null)
                            launch(Dispatchers.IO) {
                                metadata = Metadata(gson, task.result.documents[0].data!!)
                                job.complete()
                            }
                        else
                            job.complete()
                    }
                firestore.enableNetwork()
            }
        }

        invokeEvent {
            it.onUserChanged(auth.currentUser?.uid)
        }
        job.join()
    }

    override suspend operator fun plusAssign(song: Song) {
        fileRepository += song
        invokeEvent {
            it.onSongListChanged(song)
        }
    }

    override suspend operator fun plusAssign(loop: Loop) {
        metadata.loops.await().add(loop)
        invokeEvent {
            it.onLoopListChanged(loop)
        }
    }

    override suspend operator fun plusAssign(playlist: Playlist) {
        metadata.playlists.await().add(playlist)
        invokeEvent {
            it.onPlaylistListChanged(playlist)
        }
    }
}