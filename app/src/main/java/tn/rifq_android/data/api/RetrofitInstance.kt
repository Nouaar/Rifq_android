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
    
    // Track ongoing refresh to prevent concurrent refresh attempts
    @Volatile
    private var isRefreshing = false
    private val refreshLock = Any()

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
        level = HttpLoggingInterceptor.Level.BASIC  // Reduced logging for cleaner logcat
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
            .add(tn.rifq_android.data.model.notification.RecipientAdapter())
            .add(tn.rifq_android.data.model.booking.PetAdapter())
            .add(tn.rifq_android.data.model.chat.MessageAdapter())
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


    private val tokenAuthenticator = Authenticator { route: Route?, response: Response ->
        // Prevent infinite retry loops
        if (responseCount(response) >= 2) {
            runBlocking { tokenManager?.clearTokens() }
            return@Authenticator null
        }

        val manager = tokenManager ?: return@Authenticator null

        // Use synchronized block to prevent concurrent refresh attempts
        synchronized(refreshLock) {
            // Check if another thread already refreshed the token
            val currentToken = runBlocking { manager.getAccessToken().firstOrNull() }
            val originalToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            
            // If token has changed since the failed request, retry with new token
            if (currentToken != null && currentToken != originalToken) {
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }
            
            // If already refreshing, wait and retry with new token
            if (isRefreshing) {
                // Wait a bit for ongoing refresh to complete
                Thread.sleep(1000)
                val newToken = runBlocking { manager.getAccessToken().firstOrNull() }
                return@Authenticator if (newToken != null && newToken != originalToken) {
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                } else {
                    null
                }
            }
            
            isRefreshing = true
        }

        try {
            val refreshToken = runBlocking { manager.getRefreshToken().firstOrNull() }
            if (refreshToken.isNullOrEmpty()) {
                runBlocking { manager.clearTokens() }
                return@Authenticator null
            }

            // Call refresh endpoint
            val refreshResponse = try {
                runBlocking { refreshApi.refresh(RefreshRequest(refreshToken)) }
            } catch (e: Exception) {
                // Network error or server issue - don't clear tokens, might be temporary
                return@Authenticator null
            } finally {
                synchronized(refreshLock) {
                    isRefreshing = false
                }
            }

            if (refreshResponse == null || !refreshResponse.isSuccessful) {
                // Refresh failed - tokens are invalid, clear them
                runBlocking { manager.clearTokens() }
                return@Authenticator null
            }

            val tokens = refreshResponse.body()?.tokens
            if (tokens == null) {
                runBlocking { manager.clearTokens() }
                return@Authenticator null
            }

            // Persist new tokens
            runBlocking { manager.saveTokens(tokens.accessToken, tokens.refreshToken) }

            // Retry the original request with new access token
            response.request.newBuilder()
                .header("Authorization", "Bearer ${tokens.accessToken}")
                .build()
                
        } finally {
            synchronized(refreshLock) {
                isRefreshing = false
            }
        }
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

    private val aiClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .authenticator(tokenAuthenticator)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    // Retrofit for general APIs (shorter timeouts)
    val api: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val profileApi: ProfileApi by lazy { retrofit.create(ProfileApi::class.java) }
    val petsApi: PetsApi by lazy { retrofit.create(PetsApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val chatApi: ChatApi by lazy { retrofit.create(ChatApi::class.java) }
    val vetSitterApi: VetSitterApi by lazy { retrofit.create(VetSitterApi::class.java) }

    // Retrofit instance for AI endpoints (longer timeouts)
    private val aiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(aiClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // AI API uses dedicated Retrofit with longer timeouts
    val aiApi: AIApi by lazy { aiRetrofit.create(AIApi::class.java) }
    val bookingApi: BookingApi by lazy { retrofit.create(BookingApi::class.java) }
    val notificationApi: NotificationApi by lazy { retrofit.create(NotificationApi::class.java) }
    val subscriptionApi: SubscriptionApi by lazy { retrofit.create(SubscriptionApi::class.java) }
    val medicalHistoryApi: MedicalHistoryApi by lazy { retrofit.create(MedicalHistoryApi::class.java) }
}