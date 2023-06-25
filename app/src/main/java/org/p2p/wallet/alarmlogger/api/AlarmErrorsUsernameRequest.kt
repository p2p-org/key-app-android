package org.p2p.wallet.alarmlogger.api

import android.os.Build
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.p2p.wallet.BuildConfig
import org.p2p.core.crypto.Base58String

class AlarmErrorsUsernameRequest(
    @SerializedName("user_pubkey")
    val userPubkey: Base58String,
    @SerializedName("name_service_error")
    val nameServiceError: String,
    @SerializedName("name")
    val username: String,
    @SerializedName("platform")
    val platform: String = "Android ${Build.VERSION.SDK_INT}, ${Build.MANUFACTURER}, ${Build.MODEL}",
    @SerializedName("app_version")
    val appVersion: String = BuildConfig.VERSION_NAME,
    @SerializedName("timestamp")
    val timestamp: String = SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault()).format(Date()),
)
