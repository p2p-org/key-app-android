package org.p2p.wallet.history.ui.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import org.threeten.bp.ZonedDateTime
import org.p2p.uikit.utils.recycler.RoundedItem
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
}
