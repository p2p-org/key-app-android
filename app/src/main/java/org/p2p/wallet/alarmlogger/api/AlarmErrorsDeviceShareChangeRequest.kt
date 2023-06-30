package org.p2p.wallet.alarmlogger.api

import android.os.Build
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.BuildConfig

data class AlarmErrorsDeviceShareChangeRequest(
    @SerializedName("user_pubkey")
    val userPublicKey: Base58String,
    @SerializedName("platform")
    val platform: String = "Android ${Build.VERSION.SDK_INT}, ${Build.MANUFACTURER}, ${Build.MODEL}",
    @SerializedName("app_version")
    val appVersion: String = BuildConfig.VERSION_NAME,
    @SerializedName("timestamp")
    val timestamp: String = SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault()).format(Date()),
    @SerializedName("error")
    val deviceShareChangeError: DeviceShareChangeErrorRequest
)

data class DeviceShareChangeErrorRequest(
    @SerializedName("source")
    val source: String,
    @SerializedName("error")
    val error: String
)
