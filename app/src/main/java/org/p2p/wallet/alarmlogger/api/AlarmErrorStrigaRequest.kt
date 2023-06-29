package org.p2p.wallet.alarmlogger.api

import android.os.Build
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.utils.Base58String

data class AlarmErrorStrigaRequest(
    @SerializedName("user_pubkey")
    val userPublicKey: Base58String,
    @SerializedName("platform")
    val platform: String = "Android ${Build.VERSION.SDK_INT}, ${Build.MANUFACTURER}, ${Build.MODEL}",
    @SerializedName("app_version")
    val appVersion: String = BuildConfig.VERSION_NAME,
    @SerializedName("timestamp")
    val timestamp: String = SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault()).format(Date()),
    @SerializedName("error")
    val strigaError: StrigaErrorRequest
)

data class StrigaErrorRequest(
    @SerializedName("source")
    val source: String,
    @SerializedName("kyc_sdk_state")
    val kycSdkState: String,
    @SerializedName("error")
    val error: String
)
