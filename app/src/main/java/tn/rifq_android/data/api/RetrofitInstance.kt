package tn.rifq_android.data.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tn.rifq_android.data.model.auth.RefreshRequest
import tn.rifq_android.data.storage.TokenManager
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://rifq.onrender.com/"

    private var tokenManager: TokenManager? = null

    fun initialize(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()

        tokenManager?.let { manager ->
            runBlocking {
                val token = manager.getAccessToken().firstOrNull()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
            }
        }

        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Retrofit without auth to call refresh endpoint
    private val noAuthClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val noAuthRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(noAuthClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private val refreshApi: AuthApi by lazy { noAuthRetrofit.create(AuthApi::class.java) }

    // Authenticator to handle 401 by refreshing token once
    private val tokenAuthenticator = Authenticator { route: Route?, response: Response ->
        // Avoid infinite loops: if we've already attempted with a refreshed token, give up
        if (responseCount(response) >= 2) return@Authenticator null

        val manager = tokenManager ?: return@Authenticator null

        // Get refresh token
        val refreshToken = runBlocking { manager.getRefreshToken().firstOrNull() }
        if (refreshToken.isNullOrEmpty()) return@Authenticator null

        // Call refresh endpoint
        val refreshResponse = try {
            runBlocking { refreshApi.refresh(RefreshRequest(refreshToken)) }
        } catch (e: Exception) {
            null
        }

        if (refreshResponse == null || !refreshResponse.isSuccessful) {
            // Refresh failed
            return@Authenticator null
        }

        val tokens = refreshResponse.body()?.tokens ?: return@Authenticator null

        // Persist new tokens
        runBlocking { manager.saveTokens(tokens.accessToken, tokens.refreshToken) }

        // Retry the original request with new access token
        val newAccess = tokens.accessToken
        val newRequest: Request = response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
        newRequest
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .authenticator(tokenAuthenticator)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val api: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val profileApi: ProfileApi by lazy { retrofit.create(ProfileApi::class.java) }
    val petsApi: PetsApi by lazy { retrofit.create(PetsApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
}
