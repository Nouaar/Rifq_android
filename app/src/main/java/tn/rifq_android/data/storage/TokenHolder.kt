package tn.rifq_android.data.storage

/**
 * In-memory holder used by the OkHttp interceptor to attach token quickly.
 * TokenManager keeps this updated when saving/clearing token in DataStore.
 */
object TokenHolder {
    @Volatile
    var token: String? = null
}
