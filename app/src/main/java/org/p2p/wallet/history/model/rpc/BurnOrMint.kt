package org.p2p.wallet.history.model.rpc

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.p2p.core.token.TokenData
import org.p2p.core.utils.Constants.REN_BTC_SYMBOL
import org.p2p.core.utils.Constants.USD_SYMBOL
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.scaleMedium
import org.p2p.wallet.R
import org.p2p.wallet.history.model.RenBtcType
import org.p2p.wallet.transaction.model.TransactionStatus
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import java.math.BigInteger

@Parcelize
data class BurnOrMint(
    override val signature: String,
    override val date: ZonedDateTime,
    override val blockNumber: Int,
    override val status: TransactionStatus,
    val destination: String,
    val senderAddress: String,
    val iconUrl: String,
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

    fun getTokenIconUrl(): String = iconUrl

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
