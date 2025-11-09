package tn.rifq_android.util

import android.util.Base64
import org.json.JSONObject

object JwtDecoder {

    fun getUserIdFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            // Decode the payload (second part)
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val jsonObject = JSONObject(payload)

            // Try different common JWT field names for user ID
            when {
                jsonObject.has("userId") -> jsonObject.getString("userId")
                jsonObject.has("user_id") -> jsonObject.getString("user_id")
                jsonObject.has("id") -> jsonObject.getString("id")
                jsonObject.has("_id") -> jsonObject.getString("_id")
                jsonObject.has("sub") -> jsonObject.getString("sub")
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getEmailFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val jsonObject = JSONObject(payload)

            jsonObject.optString("email", null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

