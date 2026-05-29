package com.imcys.bilibilias.shared.platform.runtime

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.stringByRemovingPercentEncoding
import platform.UIKit.UIApplication
import kotlin.math.pow
import kotlin.math.roundToLong

actual fun openLink(url: String): Boolean {
    val nsUrl = NSURL.URLWithString(url) ?: return false
    return UIApplication.sharedApplication.canOpenURL(nsUrl).also { canOpen ->
        if (canOpen) {
            UIApplication.sharedApplication.openURL(
                url = nsUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null
            )
        }
    }
}

actual fun format(format: String, vararg args: Any?): String {
    var index = 0
    return "%(0?)(\\d+)?(?:\\.(\\d+))?([dfs])".toRegex().replace(format) { result ->
        val value = args.getOrNull(index++)
        val padWithZero = result.groupValues[1].isNotEmpty()
        val width = result.groupValues[2].toIntOrNull()
        val precision = result.groupValues[3].toIntOrNull()
        val type = result.groupValues[4]
        val formatted = when (type) {
            "d" -> value.toLongValue().toString()
            "f" -> value.toDoubleValue().formatFixed(precision ?: 6)
            else -> value.toString()
        }
        if (width == null) {
            formatted
        } else {
            val padding = if (padWithZero) '0' else ' '
            formatted.padStart(width, padding)
        }
    }
}

private fun Any?.toLongValue(): Long {
    return when (this) {
        is Number -> toLong()
        is String -> toLongOrNull() ?: 0L
        else -> 0L
    }
}

private fun Any?.toDoubleValue(): Double {
    return when (this) {
        is Number -> toDouble()
        is String -> toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}

private fun Double.formatFixed(precision: Int): String {
    val scale = 10.0.pow(precision)
    val rounded = (this * scale).roundToLong()
    val integerPart = rounded / scale.toLong()
    val fractionPart = (rounded % scale.toLong()).toString().padStart(precision, '0')
    return if (precision == 0) integerPart.toString() else "$integerPart.$fractionPart"
}

@Composable
actual fun rememberAppSignature(): String? = null

actual fun getAppVersion(): Pair<Long, String> {
    val bundle = NSBundle.mainBundle
    val info = bundle.infoDictionary

    // iOS versionCode 对应 CFBundleVersion (build number)
    val code = info?.get("CFBundleVersion") as? String
        ?: info?.get("CFBundleVersionString") as? String
        ?: "1"

    // iOS versionName 对应 CFBundleShortVersionString
    val name = info?.get("CFBundleShortVersionString") as? String
        ?: info?.get("CFBundleVersion") as? String
        ?: "1.0.0"

    return (code.toLongOrNull() ?: 1L) to name
}

actual fun urlEncode(url: String): String {
    return (url as NSString).stringByAddingPercentEncodingWithAllowedCharacters(
        NSCharacterSet.URLQueryAllowedCharacterSet()
    ) ?: url
}

actual fun urlDecode(url: String): String {
    return (url as NSString).stringByRemovingPercentEncoding ?: url
}
