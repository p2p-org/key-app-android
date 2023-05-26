package org.p2p.wallet.history.model.rpc

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.content.res.Resources
import android.os.Parcelable
import org.threeten.bp.ZonedDateTime
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.asNegativeUsdTransaction
import org.p2p.core.utils.asPositiveUsdTransaction
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

    protected open fun getSymbol(isNegativeOperation: Boolean): String = if (isNegativeOperation) "-" else "+"

    fun getBlockNumber(): String = blockNumber.let { "#$it" }

    @Parcelize
    data class BurnOrMint(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        val amount: RpcHistoryAmount,
        val tokenSymbol: String,
        val iconUrl: String?,
        override val type: RpcHistoryTransactionType,
        val fees: List<RpcFee>?,
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {

        @IgnoredOnParcel
        val isBurn: Boolean
            get() = type == RpcHistoryTransactionType.BURN

        @StringRes
        fun getTitle(): Int = if (isBurn) R.string.common_burn else R.string.common_mint

        fun getUsdAmount(): String = "${getFormattedAmount()}"

        fun getTotal(): String = "${getSymbol(isBurn)}${amount.total.abs().scaleMedium().formatToken()} $tokenSymbol"

        fun getFormattedTotal(scaleMedium: Boolean = false): String =
            if (scaleMedium) {
                "${amount.total.abs().scaleMedium().toPlainString()} $tokenSymbol"
            } else {
                "${amount.total.abs().scaleLong().toPlainString()} $tokenSymbol"
            }

        fun getFormattedAbsTotal(): String {
            return "${amount.total.abs().scaleLong().toPlainString()} $tokenSymbol"
        }

        override fun getSymbol(isNegativeOperation: Boolean): String {
            return if (isNegativeOperation) "" else "+"
        }

        @ColorRes
        fun getTextColor(): Int = when {
            !status.isCompleted() -> R.color.text_rose
            isBurn -> R.color.text_night
            else -> R.color.text_mint
        }

        fun getFormattedAmount(): String? = amount.totalInUsd?.abs()?.asUsdTransaction(getSymbol(isBurn))
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
        val fees: List<RpcFee>?,
        val tokenSymbol: String,
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type)

    @Parcelize
    data class CreateAccount(
        override val date: ZonedDateTime,
        override val signature: String,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        override val type: RpcHistoryTransactionType,
        val amount: RpcHistoryAmount,
        val iconUrl: String?,
        val fees: List<RpcFee>?,
        val tokenSymbol: String,
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {
        fun getFormattedTotal(scaleMedium: Boolean = false): String = if (scaleMedium) {
            "${amount.total.scaleMedium().formatToken()} $tokenSymbol"
        } else {
            "${amount.total.formatToken()} $tokenSymbol"
        }

        fun getFormattedAmount(): String? = amount.totalInUsd?.asNegativeUsdTransaction()
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
        val fees: List<RpcFee>?,
        val receiveAmount: RpcHistoryAmount,
        val sentAmount: RpcHistoryAmount,
        val sourceSymbol: String,
        val sourceIconUrl: String?,
        val destinationSymbol: String,
        val destinationIconUrl: String?,
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {

        fun getTitle(): String = "$sourceSymbol → $destinationSymbol"

        fun getReceivedUsdAmount(): String? = receiveAmount.totalInUsd?.asPositiveUsdTransaction()

        fun getSentUsdAmount(): String? = sentAmount.totalInUsd?.asNegativeUsdTransaction()

        fun getFormattedAmount() =
            "${receiveAmount.total.formatToken()} $sourceSymbol to ${sentAmount.total.formatToken()} $destinationSymbol"

        fun getFormattedAmountWithArrow(): String {
            return buildString {
                append("${receiveAmount.total.abs().formatToken()} $sourceSymbol")
                append(" → ")
                append("${sentAmount.total.formatToken()} $destinationSymbol")
            }
        }

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

        fun getSourceTotal(): String = "${receiveAmount.total.formatToken()} $sourceSymbol"

        fun getDestinationTotal(): String = "${sentAmount.total.formatToken()} $destinationSymbol"
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
        val amount: RpcHistoryAmount,
        val symbol: String,
        val destination: String,
        val counterPartyUsername: String?,
        val fees: List<RpcFee>?,
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {

        @IgnoredOnParcel
        val isSend: Boolean
            get() = type == RpcHistoryTransactionType.SEND

        fun getTokenIconUrl(): String? = iconUrl

        @DrawableRes
        fun getIcon(): Int = if (isSend) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive

        fun getTitle(resources: Resources): String {
            val (from: String, to: String) = if (isSend) {
                symbol to destination.cutMiddle()
            } else {
                destination.cutMiddle() to symbol
            }
            return resources.getString(R.string.details_transfer_format, from, to)
        }

        fun getFormattedUsernameOrAddress(): String = getUsername() ?: getAddress()

        private fun getUsername(): String? {
            return counterPartyUsername?.let { if (isSend) "To $it" else "From $it" }
        }

        private fun getAddress(): String {
            return if (isSend) "To ${destination.cutStart()}" else "From ${senderAddress.cutStart()}"
        }

        fun getFormattedFiatValue(): String? {
            return amount.totalInUsd
                ?.scaleShortOrFirstNotZero()
                ?.asUsdTransaction(getSymbol(isSend))
        }

        fun getTotalWithSymbol(): String = "${getSymbol(isSend)}${getFormattedTotal()}"

        @StringRes
        fun getTypeName(): Int = if (type == RpcHistoryTransactionType.RECEIVE) {
            if (status.isPending()) {
                R.string.transaction_history_receive_pending
            } else {
                R.string.transaction_history_receive
            }
        } else {
            if (status.isPending()) {
                R.string.transaction_history_send_pending
            } else {
                R.string.transaction_history_send
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
            "${amount.total.scaleMedium().formatToken()} $symbol"
        } else {
            "${amount.total.formatToken()} $symbol"
        }

        fun getFormattedAmount(): String? = amount.totalInUsd?.asUsdTransaction(getSymbol(isSend))
    }

    @Parcelize
    data class StakeUnstake(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        override val type: RpcHistoryTransactionType,
        val senderAddress: String,
        val iconUrl: String?,
        val amount: RpcHistoryAmount,
        val symbol: String,
        val destination: String,
        val fees: List<RpcFee>?,
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {

        @IgnoredOnParcel
        val isStake: Boolean
            get() = type == RpcHistoryTransactionType.STAKE

        fun getTokenIconUrl(): String? = iconUrl

        @DrawableRes
        fun getIcon(): Int = if (isStake) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive

        fun getAddress(): String = if (isStake) destination.cutStart() else senderAddress.cutStart()

        fun getValue(): String? = amount.totalInUsd?.scaleShortOrFirstNotZero()?.asUsdTransaction(getSymbol(isStake))

        fun getTotal(): String = "${getSymbol(isStake)}${getFormattedTotal()}"

        @StringRes
        fun getTypeName(): Int = when (type) {
            RpcHistoryTransactionType.UNSTAKE -> R.string.transaction_history_unstake
            else -> R.string.transaction_history_stake
        }

        @ColorRes
        fun getTextColor() = when {
            !status.isCompleted() -> {
                R.color.text_rose
            }
            isStake -> {
                R.color.text_night
            }
            else -> {
                R.color.text_mint
            }
        }

        fun getFormattedTotal(scaleMedium: Boolean = false): String = if (scaleMedium) {
            "${amount.total.scaleMedium().formatToken()} $symbol"
        } else {
            "${amount.total.formatToken()} $symbol"
        }

        fun getFormattedAmount(): String? = amount.totalInUsd?.asUsdTransaction(getSymbol(isStake))
    }

    @Parcelize
    data class WormholeReceive(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        override val type: RpcHistoryTransactionType,
        val amount: RpcHistoryAmount,
        val tokenSymbol: String,
        val iconUrl: String?,
        val fees: List<RpcFee>?,
        val claimKey: String
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {

        @StringRes
        fun getTitle(): Int = R.string.transaction_history_wh_claim

        @StringRes
        fun getSubtitle(): Int = if (status.isPending()) {
            R.string.transaction_history_claim_pending
        } else {
            R.string.transaction_history_receive
        }

        fun getUsdAmount(): String = "${getFormattedUsdAmount()}"

        fun getTotal(): String = "+${amount.total.scaleMedium().formatToken()} $tokenSymbol"

        fun getFormattedTotal(scaleMedium: Boolean = false): String =
            if (scaleMedium) {
                "${amount.total.scaleMedium().toPlainString()} $tokenSymbol"
            } else {
                "${amount.total.scaleLong().toPlainString()} $tokenSymbol"
            }

        @ColorRes
        fun getTextColor(): Int = when {
            status.isError() -> R.color.text_rose
            else -> R.color.text_night
        }

        fun getFormattedUsdAmount(): String? = amount.totalInUsd?.abs()?.asPositiveUsdTransaction()
    }

    @Parcelize
    data class WormholeSend(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        override val type: RpcHistoryTransactionType,
        val amount: RpcHistoryAmount,
        val sourceAddress: String,
        val tokenSymbol: String,
        val iconUrl: String?,
        val fees: List<RpcFee>?,
        val message: String
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {

        fun getUsdAmount(): String = "${getFormattedUsdAmount()}"

        @StringRes
        fun getTitle(): Int = R.string.transaction_history_wh_send

        @StringRes
        fun getSubtitle(): Int = if (status.isPending()) {
            R.string.transaction_history_send_pending
        } else {
            R.string.transaction_history_send
        }

        fun getTotal(): String = "${amount.total.scaleMedium().formatToken()} $tokenSymbol"

        fun getFormattedTotal(scaleMedium: Boolean = false): String =
            if (scaleMedium) {
                "${amount.total.scaleMedium().toPlainString()} $tokenSymbol"
            } else {
                "${amount.total.scaleLong().toPlainString()} $tokenSymbol"
            }

        fun getFormattedUsdAmount(): String? = amount.totalInUsd?.abs()?.asNegativeUsdTransaction()

        @ColorRes
        fun getTextColor(): Int = when {
            status.isError() -> R.color.text_rose
            else -> R.color.text_night
        }
    }

    @Parcelize
    data class Unknown(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        override val status: HistoryTransactionStatus,
        override val type: RpcHistoryTransactionType,
        val tokenSymbol: String,
        val amount: RpcHistoryAmount
    ) : RpcHistoryTransaction(date, signature, blockNumber, status, type) {
        fun getFormattedTotal(scaleMedium: Boolean = false): String = if (scaleMedium) {
            "${amount.total.scaleMedium().formatToken()} $tokenSymbol"
        } else {
            "${amount.total.formatToken()} $tokenSymbol"
        }

        fun getFormattedAmount(): String? = amount.totalInUsd?.asNegativeUsdTransaction()
    }
}
