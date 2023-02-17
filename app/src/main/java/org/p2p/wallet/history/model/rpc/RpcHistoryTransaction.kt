package org.p2p.wallet.history.model.rpc

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
import org.p2p.core.utils.Constants
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.asUsdTransaction
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.scaleShortOrFirstNotZero
import org.p2p.wallet.R
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.cutStart

sealed class RpcHistoryTransaction(
    override val date: ZonedDateTime,
    open val signature: String,
    open val blockNumber: Int,
    open val status: HistoryTransactionStatus,
    open val type: RpcHistoryTransactionType
) : HistoryTransaction(), Parcelable {

    override fun getHistoryTransactionId(): String {
        return signature
    }

    protected fun getSymbol(isSend: Boolean): String = if (isSend) "-" else "+"

    fun getBlockNumber(): String = blockNumber.let { "#$it" }

    @Parcelize
    data class BurnOrMint(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        val destination: String,
        val senderAddress: String,
        val iconUrl: String?,
        override val type: RpcHistoryTransactionType,
        val totalInUsd: BigDecimal?,
        val total: BigDecimal,
        val fee: BigInteger
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {

        @IgnoredOnParcel
        val isBurn: Boolean
            get() = type == RpcHistoryTransactionType.BURN

        @StringRes
        fun getTitle(): Int = if (isBurn) R.string.common_burn else R.string.common_mint

        @DrawableRes
        fun getIcon(): Int = if (isBurn) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive

        fun getValue(): String = "${getSymbol(isBurn)} ${getFormattedAmount()} ${Constants.USD_SYMBOL}"

        fun getTotal(): String = "${getSymbol(isBurn)} ${total.scaleMedium().formatToken()} ${Constants.REN_BTC_SYMBOL}"

        fun getFormattedTotal(scaleMedium: Boolean = false): String =
            if (scaleMedium) {
                "${total.scaleMedium().toPlainString()} ${Constants.REN_BTC_SYMBOL}"
            } else {
                "${total.scaleLong().toPlainString()} ${Constants.REN_BTC_SYMBOL}"
            }

        fun getFormattedAmount(): String? = totalInUsd?.asUsd()

        fun getFormattedFee(): String? = if (fee != BigInteger.ZERO) "$fee lamports" else null
    }

    @Parcelize
    data class CloseAccount(
        override val date: ZonedDateTime,
        override val signature: String,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        override val type: RpcHistoryTransactionType,
        val account: String,
        val iconUrl: String?,
        val tokenSymbol: String,
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type)

    @Parcelize
    data class CreateAccount(
        override val date: ZonedDateTime,
        override val signature: String,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        override val type: RpcHistoryTransactionType,
        val iconUrl: String?,
        val fee: BigInteger,
        val tokenSymbol: String,
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {
        fun getFormattedFee(): String? = if (fee != BigInteger.ZERO) "$fee lamports" else null
    }

    @Parcelize
    data class Swap(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        override val type: RpcHistoryTransactionType,
        val sourceAddress: String,
        val destinationAddress: String,
        val fee: BigInteger,
        val amountA: BigDecimal,
        val amountB: BigDecimal,
        val amountSentInUsd: BigDecimal?,
        val amountReceivedInUsd: BigDecimal?,
        val sourceSymbol: String,
        val sourceIconUrl: String?,
        val destinationSymbol: String,
        val destinationIconUrl: String?,
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {

        fun getTitle(): String = "$sourceSymbol → $destinationSymbol"

        fun getReceivedUsdAmount(): String? = amountReceivedInUsd?.asUsd()

        fun getSentUsdAmount(): String? = amountSentInUsd?.asUsd()

        fun getFormattedAmount() =
            "${amountA.formatToken()} $sourceSymbol to ${amountB.formatToken()} $destinationSymbol"

        fun getFormattedAmountWithArrow() =
            "${amountA.formatToken()} $sourceSymbol → ${amountB.formatToken()} $destinationSymbol"

        @StringRes
        fun getTypeName(): Int = when {
            status.isPending() -> R.string.transaction_history_swap_pending
            else -> R.string.transaction_history_swap
        }

        @ColorRes
        fun getTextColor() = when {
            status.isCompleted() -> {
                R.color.text_mint
            }
            else -> {
                R.color.text_rose
            }
        }

        fun getFormattedFee(): String? = if (fee != BigInteger.ZERO) "$fee lamports" else null

        fun getSourceTotal(): String = "${amountA.formatToken()} $sourceSymbol"

        fun getDestinationTotal(): String = "${amountB.formatToken()} $destinationSymbol"
    }

    @Parcelize
    data class Transfer(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        override val type: RpcHistoryTransactionType,
        val senderAddress: String,
        val iconUrl: String?,
        val totalInUsd: BigDecimal?,
        val symbol: String,
        val total: BigDecimal,
        val destination: String,
        val fee: BigInteger,
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {

        @IgnoredOnParcel
        val isSend: Boolean
            get() = type == RpcHistoryTransactionType.SEND

        fun getTokenIconUrl(): String? = iconUrl

        @DrawableRes
        fun getIcon(): Int = if (isSend) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive

        fun getTitle(resources: Resources): String = if (isSend) {
            resources.getString(R.string.details_transfer_format, symbol, destination.cutMiddle())
        } else {
            resources.getString(R.string.details_transfer_format, destination.cutMiddle(), symbol)
        }

        fun getAddress(): String = if (isSend) "To ${destination.cutStart()}" else "From ${senderAddress.cutStart()}"

        fun getValue(): String? = totalInUsd?.scaleShortOrFirstNotZero()?.asUsdTransaction(getSymbol(isSend))

        fun getTotal(): String = "${getSymbol(isSend)}${getFormattedTotal()}"

        fun getFormattedFee(): String? = if (fee != BigInteger.ZERO) "$fee lamports" else null

        @StringRes
        fun getTypeName(): Int = when {
            status.isPending() -> {
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
            !status.isCompleted() -> {
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
            "${total.scaleMedium().formatToken()} $symbol"
        } else {
            "${total.formatToken()} $symbol"
        }

        fun getFormattedAmount(): String? = totalInUsd?.asUsd()
    }

    @Parcelize
    data class Unknown(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        override val type: RpcHistoryTransactionType
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type)
}
