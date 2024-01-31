package org.p2p.wallet.pnl.api

import kotlin.time.Duration.Companion.days

enum class PnlDataTimeSpan(private val days: Int?) {
    LAST_24_HOURS(null);
    /* todo: currently not in use
    LAST_7_DAYS(7),
    LAST_30_DAYS(30),
    LAST_90_DAYS(90),
    LAST_365_DAYS(365);
     */

    val sinceEpochSeconds: Long?
        get() = days?.let(::daysBackToTimestamp)

    private fun daysBackToTimestamp(days: Int): Long {
        return (System.currentTimeMillis() / 1000) - days.days.inWholeSeconds
    }
}
