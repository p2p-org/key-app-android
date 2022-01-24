package org.p2p.wallet.utils

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.DateFormat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import okio.IOException
import org.p2p.wallet.R
import java.io.File
import java.io.FileOutputStream
import java.util.Date

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

fun Fragment.takeScreenShot(bitmap: Bitmap) {
    val date = Date()
    val format = DateFormat.format("MM-dd-yyyy_hh:mm:ss", date)
    try {
        val mainDir =
            File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), getString(R.string.app_name))
        if (!mainDir.exists()) {
            val mkdir = mainDir.mkdir()
        }

        val stringPath = mainDir.absolutePath + "/$date" + ".jpeg"
        val imageFile = File(stringPath)
        val fos = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        requireContext().shareScreenShoot(imageFile)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun Context.shareScreenShoot(image: File) {
    val uri = FileProvider.getUriForFile(this, this.packageName + ".provider", image)
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/*"
        putExtra(android.content.Intent.EXTRA_TEXT, "Save Screenshot")
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    try {
        startActivity(Intent.createChooser(intent, "Share with"))
    } catch (e: ActivityNotFoundException) {
        toast("No App Available")
    }
}