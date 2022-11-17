package com.aakifahamath.fyp.domain.use_case

import android.util.Log
import com.aakifahamath.fyp.common.Resource
import com.aakifahamath.fyp.data.remote.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthenticationUseCase() {

    private val auth: FirebaseAuth = Firebase.auth

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun signInAnonymousUser(): Flow<Resource<Boolean>> {
        return flow {
            emit(Resource.Loading(false))
            var success = false
            auth.signInAnonymously().addOnSuccessListener {
                Log.d(TAG, "Sign in successful")
                success = true
            }.await()

            if (success) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Failed to login", false))
            }
        }
    }

    fun signOutUser() {
        auth.signOut()
    }

}