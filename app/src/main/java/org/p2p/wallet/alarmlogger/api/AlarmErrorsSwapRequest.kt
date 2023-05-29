package org.p2p.wallet.alarmlogger.api

import android.os.Build
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.utils.Base58String

data class AlarmErrorsSwapRequest(
    @SerializedName("token_a")
    val tokenA: TokenARequest,
    @SerializedName("token_b")
    val tokenB: TokenBRequest,
    @SerializedName("route")
    val swapRouteAsJson: String,
    @SerializedName("user_pubkey")
    val userPublicKey: Base58String,
    @SerializedName("slippage")
    val slippage: String,
    @SerializedName("fee_relayer_transaction")
    val jupiterTransaction: String,
    @SerializedName("platform")
    val platform: String = "Android ${Build.VERSION.SDK_INT}, ${Build.MANUFACTURER}, ${Build.MODEL}",
    @SerializedName("app_version")
    val appVersion: String = BuildConfig.VERSION_NAME,
    @SerializedName("timestamp")
    val timestamp: String = SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault()).format(Date()),
    @SerializedName("blockchain_error")
    val blockchainError: String,
    @SerializedName("diff_routes_time")
    val diffRoutesTime: String,
    @SerializedName("diff_tx_time")
    val diffTxTime: String,
) {
    data class TokenARequest(
        @SerializedName("name")
        val tokenName: String,
        @SerializedName("mint")
        val mint: Base58String,
        @SerializedName("send_amount")
        val amount: String,
        @SerializedName("balance")
        val balance: String,
    )

    data class TokenBRequest(
        @SerializedName("name")
        val tokenName: String,
        @SerializedName("mint")
        val mint: Base58String,
        @SerializedName("expected_amount")
        val amount: String,
        @SerializedName("balance")
        val balance: String,
    )
}
