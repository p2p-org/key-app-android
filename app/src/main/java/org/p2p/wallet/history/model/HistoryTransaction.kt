package org.p2p.wallet.history.model

import android.content.Context
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.R
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.user.model.TokenData
import org.p2p.wallet.utils.Constants.REN_BTC_SYMBOL
import org.p2p.wallet.utils.Constants.USD_SYMBOL
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.scaleShort
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import java.math.BigInteger

private const val ADDRESS_SYMBOL_COUNT = 10

sealed class HistoryTransaction(
    open val date: ZonedDateTime
) : Parcelable {

    abstract val signature: String
    abstract val blockNumber: Int?

    protected fun getSymbol(isSend: Boolean): String = if (isSend) "-" else "+"

    fun getBlockNumber(): String? = blockNumber?.let { "#$it" }

    @Parcelize
    data class Swap(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int?,
        val sourceAddress: String,
        val destinationAddress: String,
        val fee: BigInteger,
        val amountA: BigDecimal,
        val amountB: BigDecimal,
        val amountSentInUsd: BigDecimal?,
        val amountReceivedInUsd: BigDecimal?,
        val sourceSymbol: String,
        val sourceIconUrl: String,
        val destinationSymbol: String,
        val destinationIconUrl: String,
        val status: TransactionStatus = TransactionStatus.COMPLETED
    ) : HistoryTransaction(date) {

        fun getTitle(): String = "$sourceSymbol → $destinationSymbol"

        fun getReceivedUsdAmount(): String? = amountReceivedInUsd?.let { "+$$it" }

        fun getSentUsdAmount(): String? = amountSentInUsd?.let { "~$$it" }

        fun getFormattedAmount() = "$amountA $sourceSymbol to $amountB $destinationSymbol"

        fun getFormattedFee() = "$fee lamports"

        fun getSourceTotal(): String = "$amountA $sourceSymbol"

        fun getDestinationTotal(): String = "$amountB $destinationSymbol"
    }

    @Parcelize
    data class Transfer(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int?,
        val type: TransferType,
        val senderAddress: String,
        val tokenData: TokenData,
        val totalInUsd: BigDecimal?,
        val total: BigDecimal,
        val destination: String,
        val fee: BigInteger,
        val status: TransactionStatus = TransactionStatus.COMPLETED
    ) : HistoryTransaction(date) {

        @IgnoredOnParcel
        val isSend: Boolean
            get() = type == TransferType.SEND

        @DrawableRes
        fun getIcon(): Int = if (isSend) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive

        fun getTitle(context: Context): String =
            if (isSend) {
                context.getString(R.string.details_transfer_format, tokenData.symbol, destination.cutMiddle())
            } else {
                context.getString(R.string.details_transfer_format, destination.cutMiddle(), tokenData.symbol)
            }

        fun getAddress(): String = if (isSend) "to ${cutAddress(destination)}" else "from ${cutAddress(senderAddress)}"

        fun getValue(): String? = totalInUsd?.let { "${getSymbol(isSend)} $${it.scaleShort()}" }

        fun getTotal(): String = "${getSymbol(isSend)} ${getFormattedTotal()}"

        @ColorInt
        fun getTextColor(context: Context) =
            if (isSend) {
                context.getColor(R.color.textIconPrimary)
            } else {
                context.getColor(R.color.systemSuccessMain)
            }

        fun getFormattedTotal(scaleMedium: Boolean = false): String =
            if (scaleMedium) {
                "${total.scaleMedium().toPlainString()} ${tokenData.symbol}"
            } else {
                "${total.scaleLong().toPlainString()} ${tokenData.symbol}"
            }

        fun getFormattedAmount(): String? = totalInUsd?.let { "~$${totalInUsd.scaleShort()}" }
    }

    @Parcelize
    data class BurnOrMint(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        val destination: String,
        val senderAddress: String,
        val type: RenBtcType,
        val totalInUsd: BigDecimal?,
        val total: BigDecimal,
        val fee: BigInteger
    ) : HistoryTransaction(date) {

        @IgnoredOnParcel
        val isBurn: Boolean
            get() = type == RenBtcType.BURN

        @StringRes
        fun getTitle(): Int = if (isBurn) R.string.main_burn_renbtc else R.string.main_mint_renbtc

        @DrawableRes
        fun getIcon(): Int = if (isBurn) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive

        fun getValue(): String = "${getSymbol(isBurn)} ${getFormattedAmount()} $USD_SYMBOL"

        fun getTotal(): String = "${getSymbol(isBurn)} ${total.scaleMedium()} $REN_BTC_SYMBOL"

        fun getFormattedTotal(scaleMedium: Boolean = false): String =
            if (scaleMedium) {
                "${total.scaleMedium().toPlainString()} $REN_BTC_SYMBOL"
            } else {
                "${total.scaleLong().toPlainString()} $REN_BTC_SYMBOL"
            }

        fun getFormattedAmount(): String? = totalInUsd?.let { "~$${totalInUsd.scaleShort()}" }
    }

    @Parcelize
    data class CreateAccount(
        override val date: ZonedDateTime,
        override val signature: String,
        override val blockNumber: Int,
        val fee: BigInteger
    ) : HistoryTransaction(date)

    @Parcelize
    data class CloseAccount(
        override val date: ZonedDateTime,
        override val signature: String,
        override val blockNumber: Int,
        val account: String,
        val mint: String,
        val tokenSymbol: String,
    ) : HistoryTransaction(date) {

        fun getInfo(): String = if (tokenSymbol.isNotBlank()) "$tokenSymbol Closed" else "Closed"
    }

    @Parcelize
    data class Unknown(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int
    ) : HistoryTransaction(date)

    @Suppress("MagicNumber")
    fun cutAddress(address: String): String {
        if (address.length < ADDRESS_SYMBOL_COUNT) {
            return address
        }

        val firstSix = address.take(4)
        val lastFour = address.takeLast(4)
        return "$firstSix...$lastFour"
    }
}
