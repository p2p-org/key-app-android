package org.p2p.wallet.common.date

import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

object Today {
    private var creationTimestamp: Long = 0L
    private var nextDayDelta: Long = 0L
    private lateinit var valueInternal: LocalDate

    val value: LocalDate
        get() {
            if (System.currentTimeMillis() - creationTimestamp > nextDayDelta) updateValue()
            return valueInternal
        }

    init {
        updateValue()
    }

    private fun updateValue() {
        val dateTime = LocalDateTime.now()
        creationTimestamp = System.currentTimeMillis()
        val tomorrow = dateTime.toLocalDate().atStartOfDay().plusDays(1)
        nextDayDelta = Duration.between(dateTime, tomorrow).toMillis()

        valueInternal = dateTime.toLocalDate()
    }
}
