package com.aakifahamath.lanes.data.authentication

import com.aakifahamath.lanes.domain.authentication.Authentication
import com.aakifahamath.lanes.util.*
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthentication @Inject constructor(
    private val auth: FirebaseAuth
) : Authentication {

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun isUserAnonymous(): Boolean {
        return auth.currentUser?.isAnonymous ?: false
    }

    override fun signOutUser() {
        auth.signOut()
    }

    override fun signInAnonymousUser(): Flow<Resource<Boolean>> {
        return callbackFlow {
            try {
                trySend(Resource.Loading(false))
                val loginTask = auth.signInAnonymously()

                withTimeout(TIMEOUT_IN_MS) {
                    loginTask.addOnSuccessListener {
                        trySend(Resource.Success(true))
                        close() // Required alongside awaitClose()
                    }.await()
                }
            } catch (e: FirebaseNetworkException) {
                trySend(Resource.Error(NETWORK_FAILURE))
                close() // Required alongside awaitClose()
            } catch (e: TimeoutCancellationException) {
                trySend(Resource.Error(TIMEOUT_FAILURE))
                close() // Required alongside awaitClose()
            }
            awaitClose() // Necessary for callbackFlow
        }
    }

    override fun signInEmailUser(email: String, password: String): Flow<Resource<Boolean>> {
        return callbackFlow {
            try {
                trySend(Resource.Loading(false))
                val loginTask = auth.signInWithEmailAndPassword(email, password)

                withTimeout(TIMEOUT_IN_MS) {
                    loginTask.addOnSuccessListener {
                        trySend(Resource.Success(true))
                        close()
                    }.await()
                }
            } catch (e: FirebaseNetworkException) {
                trySend(Resource.Error(NETWORK_FAILURE))
                close()
            } catch (e: TimeoutCancellationException) {
                trySend(Resource.Error(TIMEOUT_FAILURE))
                close()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                trySend(Resource.Error(INVALID_CREDENTIALS))
                close()
            } catch (e: FirebaseAuthInvalidUserException) {
                trySend(Resource.Error(USER_NON_EXISTENT))
                close()
            }
            awaitClose()
        }
    }

    override fun createEmailUser(email: String, password: String): Flow<Resource<Boolean>> {
        return callbackFlow {
            try {
                trySend(Resource.Loading(false))
                val signUpTask = auth.createUserWithEmailAndPassword(email, password)
                withTimeout(TIMEOUT_IN_MS) {
                    signUpTask.addOnSuccessListener {
                        trySend(Resource.Success(true))
                        close()
                    }.await()
                }
            } catch (e: FirebaseNetworkException) {
                trySend(Resource.Error(NETWORK_FAILURE))
                close()
            } catch (e: TimeoutCancellationException) {
                trySend(Resource.Error(TIMEOUT_FAILURE))
                close()
            } catch (e: FirebaseAuthUserCollisionException) {
                trySend(Resource.Error(USER_ALREADY_EXISTS))
                close()
            } catch (e: FirebaseAuthWeakPasswordException) {
                trySend(Resource.Error(WEAK_PASSWORD))
                close()
            }
            awaitClose()
        }
    }


}