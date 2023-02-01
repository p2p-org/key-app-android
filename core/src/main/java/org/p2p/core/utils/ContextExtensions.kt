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
