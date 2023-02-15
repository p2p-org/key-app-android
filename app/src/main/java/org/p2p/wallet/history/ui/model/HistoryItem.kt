package org.p2p.wallet.history.ui.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import org.threeten.bp.ZonedDateTime
import org.p2p.uikit.utils.recycler.RoundedItem

sealed interface HistoryItem {
    val date: ZonedDateTime

    data class TransactionItem(
        override val date: ZonedDateTime,
        val signature: String,
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

    data class DateItem(override val date: ZonedDateTime) : HistoryItem

    data class MoonpayTransactionItem(
        override val date: ZonedDateTime,
        val transactionId: String,

        val statusIconRes: Int,
        val statusBackgroundRes: Int,
        val statusIconColor: Int,

        val titleStatus: String,
        val subtitleReceiver: String,

        val endTopValue: String,
        val endBottomValue: String? = null,
    ) : HistoryItem, RoundedItem
}
