package com.aakifahamath.lanes.domain.authentication

import com.aakifahamath.lanes.util.Resource
import kotlinx.coroutines.flow.Flow

interface Authentication {
    fun isUserLoggedIn(): Boolean
    fun getUserId(): String?
    fun isUserAnonymous(): Boolean
    fun signOutUser()
    fun signInAnonymousUser(): Flow<Resource<Boolean>>
    fun signInEmailUser(email: String, password: String): Flow<Resource<Boolean>>
    fun createEmailUser(email: String, password: String): Flow<Resource<Boolean>>
}