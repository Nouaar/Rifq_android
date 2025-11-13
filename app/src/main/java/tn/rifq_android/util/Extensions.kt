package tn.rifq_android.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Useful Kotlin extension functions for common operations
 */

/**
 * String Extensions
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
        else it.toString()
    }
}

/**
 * Date Extensions
 */
fun Date.toFormattedString(pattern: String = "MMM dd, yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

/**
 * Number Extensions
 */
fun Int?.orDefault(default: Int = 0): Int = this ?: default

fun Double?.orDefault(default: Double = 0.0): Double = this ?: default

fun Double.format(decimals: Int = 1): String {
    return "%.${decimals}f".format(this)
}

/**
 * Collection Extensions
 */
fun <T> List<T>.isNotNullOrEmpty(): Boolean {
    return this.isNotEmpty()
}

/**
 * Boolean Extensions
 */
fun Boolean?.orFalse(): Boolean = this ?: false

fun Boolean?.orTrue(): Boolean = this ?: true

