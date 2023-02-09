package org.p2p.wallet.history.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.content.res.Resources
import android.os.Parcelable
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.TokenData
import org.p2p.core.utils.Constants.REN_BTC_SYMBOL
import org.p2p.core.utils.Constants.USD_SYMBOL
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.asUsdTransaction
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.scaleShortOrFirstNotZero
import org.p2p.wallet.R
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.utils.CUT_4_SYMBOLS
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.cutStart

private const val ADDRESS_SYMBOL_COUNT = 10

sealed class HistoryTransaction(
    open val date: ZonedDateTime
) : Parcelable {

    abstract val signature: String
    abstract val blockNumber: Int?
    abstract val status: TransactionStatus

    protected fun getSymbol(isSend: Boolean): String = if (isSend) "-" else "+"

    fun getBlockNumber(): String? = blockNumber?.let { "#$it" }

    @IgnoredOnParcel
    val isFailed: Boolean
        get() = status == TransactionStatus.ERROR

    @IgnoredOnParcel
    val isPending: Boolean
        get() = status == TransactionStatus.PENDING

    @Parcelize
    data class Swap(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int?,
        override val status: TransactionStatus,
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
    ) : HistoryTransaction(date) {

        fun getTitle(): String = "$sourceSymbol → $destinationSymbol"

        fun getReceivedUsdAmount(): String? = amountReceivedInUsd?.asUsd()

        fun getSentUsdAmount(): String? = amountSentInUsd?.asUsd()

        fun getFormattedAmount() =
            "${amountA.formatToken()} $sourceSymbol to ${amountB.formatToken()} $destinationSymbol"

        @StringRes
        fun getTypeName(): Int = when {
            isFailed -> R.string.transaction_history_swap_failed
            isPending -> R.string.transaction_history_swap_pending
            else -> R.string.transaction_history_swap
        }

        @ColorRes
        fun getTextColor() = when {
            isFailed -> {
                R.color.text_rose
            }
            else -> {
                R.color.text_mint
            }
        }

        fun getFormattedFee() = "$fee lamports"

        fun getSourceTotal(): String = "${amountA.formatToken()} $sourceSymbol"

        fun getDestinationTotal(): String = "${amountB.formatToken()} $destinationSymbol"
    }

    @Parcelize
    data class Transfer(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int?,
        override val status: TransactionStatus,
        val type: TransferType,
        val senderAddress: String,
        val tokenData: TokenData,
        val totalInUsd: BigDecimal?,
        val total: BigDecimal,
        val destination: String,
        val fee: BigInteger,
    ) : HistoryTransaction(date) {

        @IgnoredOnParcel
        val isSend: Boolean
            get() = type == TransferType.SEND

        fun getTokenIconUrl(): String? = tokenData.iconUrl

        @DrawableRes
        fun getIcon(): Int = if (isSend) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive

        fun getTitle(resources: Resources): String = if (isSend) {
            resources.getString(R.string.details_transfer_format, tokenData.symbol, destination.cutMiddle())
        } else {
            resources.getString(R.string.details_transfer_format, destination.cutMiddle(), tokenData.symbol)
        }

        fun getAddress(): String = if (isSend) "To ${destination.cutStart()}" else "From ${senderAddress.cutStart()}"

        fun getValue(): String? = totalInUsd?.scaleShortOrFirstNotZero()?.asUsdTransaction(getSymbol(isSend))

        fun getTotal(): String = "${getSymbol(isSend)}${getFormattedTotal()}"

        @StringRes
        fun getTypeName(): Int = when {
            isFailed -> {
                if (isSend) R.string.transaction_history_send_failed
                else R.string.transaction_history_receive_failed
            }
            isPending -> {
                if (isSend) R.string.transaction_history_send_pending
                else R.string.transaction_history_receive_pending
            }
            else -> {
                if (isSend) R.string.transaction_history_send
                else R.string.transaction_history_receive
            }
        }

        @ColorRes
        fun getTextColor() = when {
            isFailed -> {
                R.color.text_rose
            }
            isSend -> {
                R.color.text_night
            }
            else -> {
                R.color.text_mint
            }
        }

        fun getFormattedTotal(scaleMedium: Boolean = false): String = if (scaleMedium) {
            "${total.scaleMedium().formatToken()} ${tokenData.symbol}"
        } else {
            "${total.formatToken()} ${tokenData.symbol}"
        }

        fun getFormattedAmount(): String? = totalInUsd?.asUsd()
    }

    @Parcelize
    data class BurnOrMint(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: TransactionStatus,
        val tokenData: TokenData?,
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

        fun getTokenIconUrl(): String? = tokenData?.iconUrl

        @DrawableRes
        fun getIcon(): Int = if (isBurn) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive

        fun getValue(): String = "${getSymbol(isBurn)} ${getFormattedAmount()} $USD_SYMBOL"

        fun getTotal(): String = "${getSymbol(isBurn)} ${total.scaleMedium().formatToken()} $REN_BTC_SYMBOL"

        fun getFormattedTotal(scaleMedium: Boolean = false): String =
            if (scaleMedium) {
                "${total.scaleMedium().toPlainString()} $REN_BTC_SYMBOL"
            } else {
                "${total.scaleLong().toPlainString()} $REN_BTC_SYMBOL"
            }

        fun getFormattedAmount(): String? = totalInUsd?.asUsd()
    }

    @Parcelize
    data class CreateAccount(
        override val date: ZonedDateTime,
        override val signature: String,
        override val blockNumber: Int,
        override val status: TransactionStatus,
        val tokenData: TokenData?,
        val fee: BigInteger,
        val tokenSymbol: String,
    ) : HistoryTransaction(date) {

        fun getTokenIconUrl(): String? = tokenData?.iconUrl

        fun getInfo(operationText: String): String = if (tokenSymbol.isNotBlank()) {
            "$tokenSymbol $operationText"
        } else {
            operationText
        }
    }

    @Parcelize
    data class CloseAccount(
        override val date: ZonedDateTime,
        override val signature: String,
        override val blockNumber: Int,
        override val status: TransactionStatus,
        val tokenData: TokenData?,
        val account: String,
        val mint: String,
        val tokenSymbol: String,
    ) : HistoryTransaction(date) {

        fun getTokenIconUrl(): String? = tokenData?.iconUrl

        fun getInfo(operationText: String): String = if (tokenSymbol.isNotBlank()) {
            "$tokenSymbol $operationText"
        } else {
            operationText
        }
    }

    @Parcelize
    data class Unknown(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: TransactionStatus
    ) : HistoryTransaction(date)

    fun cutAddress(address: String): String {
        if (address.length < ADDRESS_SYMBOL_COUNT) {
            return address
        }

        val firstSix = address.take(CUT_4_SYMBOLS)
        val lastFour = address.takeLast(CUT_4_SYMBOLS)
        return "$firstSix...$lastFour"
    }
}
