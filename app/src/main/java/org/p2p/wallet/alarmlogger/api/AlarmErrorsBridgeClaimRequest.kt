package org.p2p.wallet.alarmlogger.api

import android.os.Build
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.utils.Base58String

data class AlarmErrorsBridgeClaimRequest(
    @SerializedName("token_to_claim")
    val tokenToClaim: TokenToClaim?,
    @SerializedName("user_pubkey")
    val userPubkey: Base58String,
    @SerializedName("user_eth_pubkey")
    val userEthPubkey: String,
    @SerializedName("platform")
    val platform: String = "Android ${Build.VERSION.SDK_INT}",
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
    data class TokenToClaim(
        @SerializedName("name")
        val tokenName: String,
        @SerializedName("solana_mint")
        val solanaMint: Base58String,
        @SerializedName("eth_mint")
        val ethMint: String,
        @SerializedName("claim_amount")
        val claimAmount: String
    )
}
