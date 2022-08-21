package com.tachyonmusic.user

import com.tachyonmusic.util.launch
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers


object User {

    var metadata: Metadata = Metadata()

    private var eventListener: IEventListener? = null
    private var isCreated = false

    fun create() {
        if (isCreated)
            return
        isCreated = true
        initialize { metadataUpdated ->
            if (metadataUpdated)
                eventListener?.onMetadataChanged()
        }
    }

    fun registerEventListener(listener: IEventListener) {
        eventListener = listener
    }

    fun signIn(email: String, password: String, onComplete: (Result<AuthResult>) -> Unit) {
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onComplete(Result(it))
                initialize { metadataUpdated ->
                    if (metadataUpdated)
                        eventListener?.onMetadataChanged()
                }
            }
    }

    fun register(email: String, password: String, onComplete: (Result<AuthResult>) -> Unit) {
        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onComplete(Result(it))
                initialize { }
            }
    }

    fun signOut() = Firebase.auth.signOut()

    val signedIn: Boolean
        get() = Firebase.auth.currentUser != null

    private fun initialize(onDone: (Boolean /*metadataUpdated*/) -> Unit) {
        if (signedIn) {
            Firebase.firestore.collection("users")
                .document(Firebase.auth.currentUser!!.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.result.data != null)
                        launch(Dispatchers.IO) {
                            val previous = metadata
                            metadata = Metadata(task.result.data!!, previous.onHistoryChanged)
                            onDone(previous != metadata)
                        }
                    else
                        onDone(false)
                }

        } else {
            Firebase.firestore.disableNetwork().addOnCompleteListener {
                Firebase.firestore.collection("users")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.result.documents.isNotEmpty() && task.result.documents[0].data != null)
                            launch(Dispatchers.IO) {
                                val previous = metadata
                                metadata = Metadata(
                                    task.result.documents[0].data!!,
                                    previous.onHistoryChanged
                                )
                                onDone(previous != metadata)
                            }
                        else
                        /**
                         * This means that the user has never signed in before. Thus we set [metadataUpdated]
                         * to true to indicate that the default metadata will be used
                         */
                            onDone(true)
                    }
                Firebase.firestore.enableNetwork()
            }
        }
    }

    // TODO: Use [Firebase.firestore.update] (https://firebase.google.com/docs/firestore/manage-data/add-data#update-data) && (https://firebase.google.com/docs/firestore/manage-data/add-data#update_elements_in_an_array)

    fun upload() {
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


    class Result<T>(task: Task<T>) {
        val isSuccessful = task.isSuccessful
        val exception = task.exception
    }

    interface IEventListener {
        fun onMetadataChanged()
    }

    class EventListener : IEventListener {
        override fun onMetadataChanged() {}
    }
}