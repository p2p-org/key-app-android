package org.p2p.wallet.history.model

import android.content.Context
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import org.p2p.wallet.R
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.user.model.TokenData
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.scaleMedium
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.utils.scaleShort
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import java.math.BigInteger

private const val ADDRESS_SYMBOL_COUNT = 10

sealed class HistoryTransaction(
    open val date: ZonedDateTime
) : Parcelable {

    abstract val signature: String
    abstract val blockNumber: Int

    protected fun getSymbol(isSend: Boolean) = if (isSend) "-" else "+"

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
    ) : HistoryTransaction(date) {

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
    ) : HistoryTransaction(date) {

        @IgnoredOnParcel
        private val isSend = type == TransferType.SEND

        @DrawableRes
        fun getIcon(): Int = if (isSend) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive

        @StringRes
        fun getTitle(): Int = if (isSend) R.string.main_transfer else R.string.main_receive

        fun getAddress(): String = if (isSend) "to ${cutAddress(destination)}" else "from ${cutAddress(senderAddress)}"

        fun getValue(): String = "${getSymbol(isSend)} ${getFormattedAmount()}"

        fun getTotal(): String = "${getSymbol(isSend)} ${getFormattedTotal()}"

        fun getFormattedTotal(): String = "${total.scaleMedium()} ${tokenData.symbol}"

        fun getFormattedAmount(): String = "${amount.scaleShort()} $"

        @ColorInt
        fun getTextColor(context: Context) =
            if (isSend) {
                context.colorFromTheme(R.attr.colorMessagePrimary)
            } else {
                ContextCompat.getColor(context, R.color.colorGreen)
            }
    }

    @Parcelize
    data class BurnOrMint(
        override val signature: String,
        override val date: ZonedDateTime,
        override val blockNumber: Int,
        val type: RenBtcType,
        val amount: BigDecimal,
        val total: BigDecimal,
        val fee: BigInteger
    ) : HistoryTransaction(date) {

        @IgnoredOnParcel
        private val isBurn = type == RenBtcType.BURN

        @StringRes
        fun getTitle(): Int = if (isBurn) R.string.main_burn_renbtc else R.string.main_mint_renbtc

        @DrawableRes
        fun getIcon(): Int = if (isBurn) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive

        fun getValue(): String = "${getSymbol(isBurn)} ${amount.scaleMedium()} $"

        fun getTotal(): String = "${getSymbol(isBurn)} ${total.scaleMedium()} ${Token.REN_BTC_SYMBOL}"
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