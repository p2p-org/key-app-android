package org.p2p.wallet.utils

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import org.p2p.wallet.R

@SuppressLint("MissingPermission")
fun Context.vibrate(duration: Long = 500) {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createOneShot(
            duration,
            VibrationEffect.DEFAULT_AMPLITUDE
        )
        vibrator?.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(duration)
    }
}

fun Context.copyToClipBoard(content: String) {
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(getString(R.string.app_name), content)
    clipboard.setPrimaryClip(clip)
}

fun Context.getClipBoardData(): CharSequence? {
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    return if (clipboard.hasPrimaryClip())
        clipboard.primaryClip?.getItemAt(0)?.text
    else
        null
}

fun Context.clearClipBoard() {
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.clearPrimaryClip()
}

fun Context.shareText(value: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, value)
    startActivity(Intent.createChooser(shareIntent, "Share Text"))
}