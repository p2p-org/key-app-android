package org.p2p.wallet.history.model

import androidx.annotation.StringRes
import org.p2p.wallet.R

enum class PeriodHistory(@StringRes val resourceId: Int, val value: Int) {
    ONE_HOUR(R.string.details_1h, 1),
    FOUR_HOURS(R.string.details_4h, 4),
    ONE_DAY(R.string.details_1d, 1),
    ONE_WEEK(R.string.details_1w, 7),
    ONE_MONTH(R.string.details_1m, 30);

    companion object {
        fun parse(tabId: Int): PeriodHistory =
            when (tabId) {
                ONE_HOUR.resourceId -> ONE_HOUR
                FOUR_HOURS.resourceId -> FOUR_HOURS
                ONE_DAY.resourceId -> ONE_DAY
                ONE_WEEK.resourceId -> ONE_WEEK
                ONE_MONTH.resourceId -> ONE_MONTH
                else -> throw IllegalStateException("Unknown tabId $tabId")
            }
    }
}
