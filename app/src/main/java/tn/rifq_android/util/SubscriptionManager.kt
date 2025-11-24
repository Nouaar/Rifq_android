package tn.rifq_android.util

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tn.rifq_android.data.model.subscription.Subscription
import tn.rifq_android.data.model.subscription.SubscriptionStatus
import tn.rifq_android.data.repository.SubscriptionRepository
import tn.rifq_android.data.storage.TokenManager
import java.util.Date

/**
 * Subscription Manager
 * iOS Reference: SubscriptionManager.swift (lines 1-91)
 * 
 * Manages subscription status checking and alerts:
 * - Periodic checking (every 30 minutes)
 * - Check on app foreground
 * - Alert cooldown (1 hour)
 * - Expiration alerts
 */
object SubscriptionManager {
    private const val TAG = "SubscriptionManager"
    private const val CHECK_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes
    private const val ALERT_COOLDOWN_MS = 60 * 60 * 1000L // 1 hour
    
    private var repository: SubscriptionRepository? = null
    private var tokenManager: TokenManager? = null
    private var checkJob: Job? = null
    private var lastAlertShownDate: Date? = null
    
    private val _subscription = MutableStateFlow<Subscription?>(null)
    val subscription: StateFlow<Subscription?> = _subscription.asStateFlow()
    
    private val _showExpirationAlert = MutableStateFlow(false)
    val showExpirationAlert: StateFlow<Boolean> = _showExpirationAlert.asStateFlow()
    
    private val _expirationMessage = MutableStateFlow<String?>(null)
    val expirationMessage: StateFlow<String?> = _expirationMessage.asStateFlow()
    
    /**
     * Initialize the subscription manager
     */
    fun initialize(
        repository: SubscriptionRepository,
        tokenManager: TokenManager
    ) {
        this.repository = repository
        this.tokenManager = tokenManager
        startPeriodicCheck()
    }
    
    /**
     * Start periodic subscription checking
     * iOS Reference: SubscriptionManager.swift line 28
     */
    fun startPeriodicCheck() {
        stopPeriodicCheck() // Stop any existing check
        
        checkJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(CHECK_INTERVAL_MS)
                checkSubscriptionStatus()
            }
        }
        
        Log.d(TAG, "✅ Started periodic subscription check (every 30 minutes)")
    }
    
    /**
     * Stop periodic checking
     * iOS Reference: SubscriptionManager.swift line 36
     */
    fun stopPeriodicCheck() {
        checkJob?.cancel()
        checkJob = null
        Log.d(TAG, "⏹️ Stopped periodic subscription check")
    }
    
    /**
     * Check subscription status and show alerts if needed
     * iOS Reference: SubscriptionManager.swift line 43
     */
    suspend fun checkSubscriptionStatus() {
        val repo = repository ?: run {
            Log.w(TAG, "⚠️ Repository not initialized")
            return
        }
        
        val token = tokenManager?.getAccessToken()?.firstOrNull()
        if (token.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ No access token available")
            return
        }
        
        try {
            val sub = repo.getSubscription()
            _subscription.value = sub
            
            // Check if subscription is expiring soon
            if (sub.subscriptionStatus == SubscriptionStatus.ACTIVE && sub.willExpireSoon) {
                val days = sub.daysUntilExpiration
                if (days != null && days > 0) {
                    // Only show alert if we haven't shown it recently (cooldown)
                    val now = Date()
                    if (lastAlertShownDate != null) {
                        val timeSinceLastAlert = now.time - lastAlertShownDate!!.time
                        if (timeSinceLastAlert < ALERT_COOLDOWN_MS) {
                            Log.d(TAG, "⏸️ Alert still in cooldown period")
                            return // Still in cooldown period
                        }
                    }
                    
                    _expirationMessage.value = "Your subscription expires in $days day${if (days == 1) "" else "s"}. Renew now to continue your service."
                    _showExpirationAlert.value = true
                    lastAlertShownDate = now
                    Log.d(TAG, "⚠️ Showing expiration alert: $days days remaining")
                }
            }
            
            // Check if subscription has expired
            if (sub.isExpired && sub.subscriptionStatus == SubscriptionStatus.ACTIVE) {
                // Show expired alert regardless of cooldown
                _expirationMessage.value = "Your subscription has expired. Your role has been downgraded to owner."
                _showExpirationAlert.value = true
                lastAlertShownDate = Date()
                Log.d(TAG, "❌ Subscription expired")
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Failed to check subscription status: ${e.message}", e)
        }
    }
    
    /**
     * Refresh subscription data
     * iOS Reference: SubscriptionManager.swift line 86
     */
    suspend fun refreshSubscription() {
        checkSubscriptionStatus()
    }
    
    /**
     * Dismiss expiration alert
     */
    fun dismissExpirationAlert() {
        _showExpirationAlert.value = false
    }
    
    /**
     * Clear all state (e.g., on logout)
     */
    fun clear() {
        stopPeriodicCheck()
        _subscription.value = null
        _showExpirationAlert.value = false
        _expirationMessage.value = null
        lastAlertShownDate = null
        repository = null
        tokenManager = null
    }
}

