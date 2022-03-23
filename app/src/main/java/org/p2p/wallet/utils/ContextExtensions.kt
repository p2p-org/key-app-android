package org.p2p.wallet.utils

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.FileProvider
import org.p2p.wallet.R
import java.io.File

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

fun Context.copyToClipBoard(content: String) {
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(getString(R.string.app_name), content)
    clipboard.setPrimaryClip(clip)
}

fun Context.getClipBoardText(trimmed: Boolean = true): String? {
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
    return if (trimmed) text?.trim() else text
}

fun Context.shareText(value: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, value)
    startActivity(Intent.createChooser(shareIntent, "Share Text"))
}

fun Context.shareScreenShot(image: File, providedText: String = "Save Screenshot") {
    val uri = FileProvider.getUriForFile(this, this.packageName + ".provider", image)
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/*"
        putExtra(Intent.EXTRA_TEXT, providedText)
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    try {
        startActivity(Intent.createChooser(intent, "Share with"))
    } catch (e: ActivityNotFoundException) {
        toast("No App Available")
    }
}
