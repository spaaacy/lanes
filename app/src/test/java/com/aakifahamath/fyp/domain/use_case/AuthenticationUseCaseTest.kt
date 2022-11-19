package com.aakifahamath.fyp.domain.use_case

import android.app.Activity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.Exception
import java.util.concurrent.Executor

class AuthenticationUseCaseTest {

    private val authenticationUseCase = mockk<AuthenticationUseCase>()
    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val task = mockk<Task<AuthResult>>()

    @Before
    fun setUp() {
        every { firebaseAuth.currentUser } returns null
        every { firebaseAuth.signInAnonymously() } returns task
        every { firebaseAuth.signOut() } returns Unit
        every { authenticationUseCase getProperty "auth" } returns firebaseAuth
    }

    @Test
    fun `test 1`() = runBlocking {
        if (!authenticationUseCase.isUserLoggedIn()) {
            println("User is null")
    }

    @Test
    fun `Order notes by title ascending, correct order`() = runBlocking {

    }


}
}


















