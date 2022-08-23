package com.tachyonmusic.user.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tachyonmusic.user.Metadata
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.launch
import kotlinx.coroutines.Dispatchers

class FirebaseRepository : UserRepository {
    override var metadata: Metadata = Metadata()
        private set

    private var eventListener: UserRepository.EventListener? = null

    override fun signIn(
        email: String,
        password: String,
        onComplete: (Boolean /*isSuccessful*/, String? /*errorMsg*/) -> Unit
    ) {
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onComplete(it.isSuccessful, it.exception?.message)
                initialize { metadataUpdated ->
                    if (metadataUpdated)
                        eventListener?.onMetadataChanged()
                }
                println("User $email and $password signed in: ${it.isSuccessful}")
            }
    }

    override fun register(
        email: String,
        password: String,
        onComplete: (Boolean /*isSuccessful*/, String? /*errorMsg*/) -> Unit
    ) {
        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onComplete(it.isSuccessful, it.exception?.message)
                initialize()
            }
    }

    override fun signOut() = Firebase.auth.signOut()

    override val signedIn: Boolean
        get() = Firebase.auth.currentUser != null

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

    private fun initialize(onDone: ((Boolean /*metadataUpdated*/) -> Unit)? = null) {
        if (signedIn) {
            Firebase.firestore.collection("users")
                .document(Firebase.auth.currentUser!!.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.result.data != null)
                        launch(Dispatchers.IO) {
                            val previous = metadata
                            metadata = Metadata(task.result.data!!, previous.onHistoryChanged)
                            onDone?.invoke(previous != metadata)
                        }
                    else
                        onDone?.invoke(false)
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
                                onDone?.invoke(previous != metadata)
                            }
                        else
                        /**
                         * This means that the user has never signed in before. Thus we set [metadataUpdated]
                         * to true to indicate that the default metadata will be used
                         */
                            onDone?.invoke(true)
                    }
                Firebase.firestore.enableNetwork()
            }
        }
    }
}