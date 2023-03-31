package org.p2p.wallet.history.ui.delegates

import android.content.Context
import org.threeten.bp.ZonedDateTime
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.date.toDateString

data class HistoryDateCellModel(
    val date: ZonedDateTime
) : AnyCellItem {
    fun getFormattedDate(context: Context): String = date.toDateString(context)
}
