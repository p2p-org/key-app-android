package org.p2p.wallet.history.ui.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import org.threeten.bp.ZonedDateTime
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.scaleShortOrFirstNotZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.asUsdTransaction
import org.p2p.uikit.utils.recycler.RoundedItem
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.utils.emptyString

sealed interface HistoryItem {
    val date: ZonedDateTime
    val transactionId: String

    data class TransactionItem(
        override val date: ZonedDateTime,
        override val transactionId: String,
        val tokenIconUrl: String?,
        val sourceIconUrl: String?,
        val destinationIconUrl: String?,

        val startTitle: String?,
        val startSubtitle: String?,
        val endTopValue: String?,
        @ColorRes val endTopValueTextColor: Int?,
        val endBottomValue: String?,

        @DrawableRes val iconRes: Int,
        val statusIcon: Int?
    ) : HistoryItem, RoundedItem

    data class DateItem(
        override val date: ZonedDateTime,
        // TODO refactor this
        override val transactionId: String = emptyString()
    ) : HistoryItem

    data class MoonpayTransactionItem(
        override val date: ZonedDateTime,
        override val transactionId: String,

        val statusIconRes: Int,
        val statusBackgroundRes: Int,
        val statusIconColor: Int,

        val titleStatus: String,
        val subtitleReceiver: String,

        val endTopValue: String,
        val endBottomValue: String? = null,
    ) : HistoryItem, RoundedItem

    data class UserSendLinksItem(
        val linksCount: Int
    ) : HistoryItem, RoundedItem {
        override val date: ZonedDateTime = ZonedDateTime.now()
        override val transactionId: String = emptyString()
    }

    data class SwapBannerItem(
        val sourceTokenMintAddress: String,
        val destinationTokenMintAddress: String,
        val sourceTokenSymbol: String,
        val destinationTokenSymbol: String,
        val openedFrom: SwapOpenedFrom = SwapOpenedFrom.HISTORY_SCREEN_BANNER
    ) : HistoryItem {
        override val date: ZonedDateTime = ZonedDateTime.now()
        override val transactionId: String = emptyString()
    }

    data class BridgeSendItem(
        val id: String,
        val sendDetails: BridgeSendTransactionDetails
    ) : HistoryItem, RoundedItem {
        override val date: ZonedDateTime = ZonedDateTime.now()
        override val transactionId: String = id
    }

    data class BridgeClaimItem(
        val bundleId: String,
        val bundle: BridgeBundle
    ) : HistoryItem, RoundedItem {
        override val date: ZonedDateTime = ZonedDateTime.now()
        override val transactionId: String = bundleId

        fun getFormattedFiatValue(): String {
            return bundle.resultAmount.amountInUsd.toBigDecimalOrZero()
                .scaleShortOrFirstNotZero()
                .asUsdTransaction("+")
        }

        fun getFormattedTotal(scaleMedium: Boolean = false): String {
            val totalAmount = bundle.resultAmount.amountInToken
            val tokenSymbol = bundle.resultAmount.symbol

            return if (scaleMedium) {
                "${totalAmount.scaleMedium().formatToken()} $tokenSymbol"
            } else {
                "${totalAmount.formatToken()} $tokenSymbol"
            }
        }
    }
}
