package org.p2p.wallet.history.model.rpc

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import kotlinx.android.parcel.Parcelize
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.formatToken
import org.p2p.wallet.R
import org.p2p.wallet.transaction.model.TransactionStatus
import org.threeten.bp.ZonedDateTime
import java.math.BigDecimal
import java.math.BigInteger

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
