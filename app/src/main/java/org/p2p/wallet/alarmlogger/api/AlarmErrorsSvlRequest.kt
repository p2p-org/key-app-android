package org.p2p.wallet.alarmlogger.api

import android.os.Build
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.p2p.wallet.BuildConfig
import org.p2p.core.crypto.Base58String

data class AlarmErrorsSvlRequest(
    @SerializedName("token_to_claim")
    val tokenToClaim: TokenToSend?,
    @SerializedName("token_to_send")
    val tokenToSend: TokenToSend?,
    @SerializedName("user_pubkey")
    val userPubkey: Base58String,
    @SerializedName("platform")
    val platform: String = "Android ${Build.VERSION.SDK_INT}, ${Build.MANUFACTURER}, ${Build.MODEL}",
    @SerializedName("app_version")
    val appVersion: String = BuildConfig.VERSION_NAME,
    @SerializedName("timestamp")
    val timestamp: String = SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault()).format(Date()),
    @SerializedName("simulation_error")
    val simulationError: String,
    @SerializedName("fee_relayer_error")
    val feeRelayerError: String,
    @SerializedName("blockchain_error")
    val blockchainError: String
) {
    data class TokenToSend(
        @SerializedName("name")
        val tokenName: String,
        @SerializedName("mint")
        val mint: Base58String,
        @SerializedName("send_amount")
        val amount: String,
        @SerializedName("currency")
        val currency: String
    )
}
