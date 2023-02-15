package org.p2p.wallet.history.model.rpc

import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.asUsdTransaction
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.scaleShortOrFirstNotZero
import org.p2p.wallet.R
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.cutStart
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import java.math.BigInteger

@Parcelize
data class Transfer(
    override val signature: String,
    override val date: ZonedDateTime,
    override val blockNumber: Int?,
    override val status: TransactionStatus,
    val type: TransferType,
    val senderAddress: String,
    val iconUrl: String,
    val totalInUsd: BigDecimal?,
    val symbol: String,
    val total: BigDecimal,
    val destination: String,
    val fee: BigInteger,
) : HistoryTransaction(date) {

    @IgnoredOnParcel
    val isSend: Boolean
        get() = type == TransferType.SEND

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
        "${total.scaleMedium().formatToken()} $symbol"
    } else {
        "${total.formatToken()} $symbol"
    }

    fun getFormattedAmount(): String? = totalInUsd?.asUsd()
}
