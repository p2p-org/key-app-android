package com.p2p.wallet.history.model

import android.os.Parcelable
import com.p2p.wallet.user.model.TokenData
import com.p2p.wallet.utils.scaleAmount
import kotlinx.parcelize.Parcelize
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import java.math.BigInteger

sealed class Transaction(
    open val date: ZonedDateTime
) : Parcelable {

    abstract val signature: String
    abstract val blockNumber: Int

    @Parcelize
    data class Swap(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        val sourceAddress: String,
        val destinationAddress: String,
        val fee: BigInteger,
        val amountA: BigDecimal,
        val amountB: BigDecimal,
        val amountReceivedInUsd: BigDecimal,
        val sourceSymbol: String,
        val sourceTokenUrl: String,
        val destinationSymbol: String,
        val destinationTokenUrl: String
    ) : Transaction(date) {

        fun getFormattedAmount() = "$amountA $sourceSymbol to $amountB $destinationSymbol"

        fun getFormattedFee() = "$fee lamports"
    }

    @Parcelize
    data class Transfer(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        val type: TransferType,
        val senderAddress: String,
        val tokenData: TokenData,
        val amount: BigDecimal,
        val total: BigDecimal,
        val destination: String,
        val fee: BigInteger
    ) : Transaction(date) {

        fun getFormattedAmount(): String = "${amount.scaleAmount()} $"

        fun getFormattedTotal(): String = "${total.scaleAmount()} ${tokenData.symbol}"
    }

    @Parcelize
    data class CloseAccount(
        override val date: ZonedDateTime,
        override val signature: String,
        override val blockNumber: Int,
        val account: String,
        val destination: String,
        val owner: String,
        val tokenSymbol: String,
    ) : Transaction(date)

    @Parcelize
    data class Unknown(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int
    ) : Transaction(date)
}