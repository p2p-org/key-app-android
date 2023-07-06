package org.p2p.core.utils

import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Spanned
import timber.log.Timber
import org.p2p.core.BuildConfig

@SuppressLint("MissingPermission")
fun Context.vibrate(duration: Long = 500) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    val effect = VibrationEffect.createOneShot(
        duration,
        VibrationEffect.DEFAULT_AMPLITUDE
    )
    vibrator?.vibrate(effect)
}

fun Context.getHtmlString(@StringRes id: Int, vararg args: Any): Spanned =
    HtmlCompat.fromHtml(
        if (args.isNotEmpty()) getString(id, *args) else getString(id),
        HtmlCompat.FROM_HTML_MODE_LEGACY
    )

fun Resources.getHtmlString(@StringRes id: Int, vararg args: Any): Spanned =
    HtmlCompat.fromHtml(
        if (args.isNotEmpty()) getString(id, *args) else getString(id),
        HtmlCompat.FROM_HTML_MODE_LEGACY
    )


/**
 * Use this is very rare cases if you don't have an opportunity to get the string resource
 * in a common way like R.string.foo
 * */
@SuppressLint("DiscouragedApi")
fun Context.getStringResourceByName(resourceName: String): String {
    val resId = resources.getIdentifier(resourceName, "string", packageName)
    return try {
        getString(resId)
    } catch (e: Resources.NotFoundException) {
        if (!BuildConfig.DEBUG) Timber.e(e, "String resource $resourceName is not found")
        emptyString()
    }
}
