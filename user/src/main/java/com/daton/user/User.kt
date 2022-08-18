package com.daton.user

import com.daton.util.launch
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers


object User {
    var metadata: Metadata = Metadata()

    private var onSignInCallback: (() -> Unit)? = null

    fun signIn(email: String, password: String, onComplete: (Result<AuthResult>) -> Unit) {
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onComplete(Result(it))
                initialize {
                    onSignInCallback?.invoke()
                }
            }
    }

    fun register(email: String, password: String, onComplete: (Result<AuthResult>) -> Unit) {
        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onComplete(Result(it))
                initialize {

                }
            }
            .addOnCanceledListener {
                TODO("Handle Firebase register cancelled")
            }
            .addOnFailureListener {
                TODO("Handle Firebase failure")
            }
    }

    fun signOut() = Firebase.auth.signOut()

    fun onSignIn(action: () -> Unit) {
        onSignInCallback = action
    }

    private fun initialize(onDone: () -> Unit) {
        Firebase.firestore.collection("users")
            .document(Firebase.auth.currentUser!!.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.result.data != null)
                    launch(Dispatchers.IO) {
                        metadata = Metadata(task.result.data!!)
                        onDone()
                    }
                else
                    onDone()
            }
    }

    // TODO: Use [Firebase.firestore.update] (https://firebase.google.com/docs/firestore/manage-data/add-data#update-data) && (https://firebase.google.com/docs/firestore/manage-data/add-data#update_elements_in_an_array)

    fun upload() {
        Firebase.firestore.collection("users")
            .document(Firebase.auth.currentUser!!.uid)
            .set(metadata.toHashMap())
    }


    class Result<T>(task: Task<T>) {
        val isSuccessful = task.isSuccessful
        val exception = task.exception
    }
}