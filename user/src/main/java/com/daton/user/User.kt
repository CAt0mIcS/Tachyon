package com.daton.user

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth


object User {
    fun signIn(email: String, password: String, onComplete: (Result<AuthResult>) -> Unit) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onComplete(Result(it))
            }
    }

    fun register(email: String, password: String, onComplete: (Result<AuthResult>) -> Unit) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onComplete(Result(it))
            }
            .addOnCanceledListener {
                TODO("Handle Firebase register cancelled")
            }
            .addOnFailureListener {
                TODO("Handle Firebase failure")
            }
    }

    fun signOut() = FirebaseAuth.getInstance().signOut()


    class Result<T>(task: Task<T>) {
        val isSuccessful = task.isSuccessful
        val exception = task.exception
    }
}