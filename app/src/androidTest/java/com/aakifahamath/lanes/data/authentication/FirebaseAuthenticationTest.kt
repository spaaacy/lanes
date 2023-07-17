package com.aakifahamath.lanes.data.authentication

import androidx.compose.ui.test.junit4.createComposeRule
import com.aakifahamath.lanes.util.*
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import org.junit.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import javax.inject.Inject

@HiltAndroidTest
class FirebaseAuthenticationTest {

    sealed interface FirebaseRestApi {
        @DELETE(ACCOUNT_DELETE)
        suspend fun clearAccounts()

        @POST(ACCOUNT_CREATE)
        suspend fun signUpAccount(@Body requestBody: RequestBody): Response<ResponseBody>
    }

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAuthentication: FirebaseAuthentication
    private val firebaseRestApi = Retrofit.Builder()
        .baseUrl(AUTH_LOCAL_URL)
        .build().create(FirebaseRestApi::class.java)

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setup() {
        runBlocking {
            hiltRule.inject()
            firebaseRestApi.clearAccounts()
            firebaseAuth.signOut()
            firebaseAuthentication = FirebaseAuthentication(firebaseAuth)
        }
    }

    private val testEmail = "test@abc.com"
    private val testPassword = "test_password@1234"

    // signInAnonymousUser()
    @Test
    fun shouldSignInAnonymousUser_whenUserMakesRequest() {
        runBlocking {
            firebaseAuthentication.signInAnonymousUser().collect { result ->
                when(result) {
                    is Resource.Loading -> {
                        assertThat(result.data).isFalse()
                        assertThat(result.message).isNull()
                        // We don't check for currentUser != null since this can change during execution of assertion
                    }
                    is Resource.Success, is Resource.Error -> { // Resource.Error is placed here as it should fail the assertions listed
                        assertThat(result.data).isTrue()
                        assertThat(result.message).isNull()
                        assertThat(firebaseAuth.currentUser).isNotEqualTo(null)
                    }
                }
            }
        }
    }

    // signInEmailUser()
    @Test
    fun shouldSignInEmailUser_whenUserMakesRequest() {
        runBlocking {
            // Create an account in emulator using the REST API
            val newUser = JSONObject().apply {
                put(EMAIL, testEmail)
                put(PASSWORD, testPassword)
                put(RETURN_SCORE_TOKEN, true)
            }
            val newUserString = newUser.toString()
            val requestBody = newUserString.toRequestBody("application/json".toMediaTypeOrNull())
            firebaseRestApi.signUpAccount(requestBody)

            firebaseAuthentication.signInEmailUser(testEmail, testPassword).collect { result ->
                when(result) {
                    is Resource.Loading -> {
                        assertThat(result.data).isFalse()
                        assertThat(result.message).isNull()
                        // We don't check for currentUser != null since this can change during execution of assertion
                    }
                    is Resource.Success, is Resource.Error -> { // Resource.Error is placed here as it should fail the assertions listed
                        assertThat(result.data).isTrue()
                        assertThat(result.message).isNull()
                        assertThat(firebaseAuth.currentUser).isNotEqualTo(null)
                    }
                }

            }
        }
    }

    // createEmailUser()
    @Test
    fun createEmailUser() {
        runBlocking {
            firebaseAuthentication.createEmailUser(testEmail, testPassword).collect { result ->
                when(result) {
                    is Resource.Loading -> {
                        assertThat(result.data).isFalse()
                        assertThat(result.message).isNull()
                        // We don't check for currentUser != null since this can change during execution of assertion
                    }
                    is Resource.Success, is Resource.Error -> { // Resource.Error is placed here as it should fail the assertions listed
                        assertThat(result.data).isTrue()
                        assertThat(result.message).isNull()
                        assertThat(firebaseAuth.currentUser).isNotEqualTo(null)
                    }
                }

            }
        }
    }


}