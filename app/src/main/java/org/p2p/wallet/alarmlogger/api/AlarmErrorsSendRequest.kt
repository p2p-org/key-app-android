package org.p2p.wallet.alarmlogger.api

import android.os.Build
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.p2p.wallet.BuildConfig
import org.p2p.core.crypto.Base58String

data class AlarmErrorsSendRequest(
    @SerializedName("token_to_send")
    val tokenToSend: TokenToSend,
    @SerializedName("fees")
    val fees: Fees,
    @SerializedName("relay_account_state")
    val relayAccountState: RelayAccountStateRequest,
    @SerializedName("user_pubkey")
    val userPubkey: Base58String,
    @SerializedName("recipient_pubkey")
    val recipientPubkey: Base58String,
    @SerializedName("recipient_name")
    val recipientName: String,
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

    data class Fees(
        @SerializedName("transaction_fee_amount")
        val transactionFeeAmount: String,
        @SerializedName("account_creation_fee")
        val accountCreationFee: AccountCreationFee?
    )

    data class AccountCreationFee(
        @SerializedName("payment_token")
        val paymentToken: PaymentToken,
        @SerializedName("amount")
        val amount: String
    )

    data class PaymentToken(
        @SerializedName("name")
        val name: String,
        @SerializedName("mint")
        val mint: Base58String
    )

    data class RelayAccountStateRequest(
        @SerializedName("created")
        val created: Boolean,
        @SerializedName("balance")
        val balance: String
    )
}
