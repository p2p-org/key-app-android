package org.p2p.wallet.common.date

import android.content.Context
import org.p2p.wallet.R
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalAccessor
import java.util.Locale
import kotlin.math.ceil

private val monthDayFormatter = DateTimeFormatter.ofPattern("MMMM dd")
private val monthDayYearFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd.yyyy HH:mm")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun ZonedDateTime.toDateString(context: Context): String {
    val day = withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
    val today = Today.value
    return when {
        day.isEqual(today) -> context.getString(R.string.details_today)
        today.year == day.year -> monthDayFormatter.formatWithLocale(day)
        else -> monthDayYearFormatter.formatWithLocale(day)
    }
}

fun ZonedDateTime.toDateTimeString(): String {
    val localDateTime = withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    return dateTimeFormatter.formatWithLocale(localDateTime)
}

fun ZonedDateTime.toTimeString(): String {
    val localDateTime = withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    return timeFormatter.formatWithLocale(localDateTime)
}

fun ZonedDateTime.isSameDayAs(other: ZonedDateTime) =
    toLocalDate() == other.toLocalDate()

fun ZonedDateTime.isSameAs(other: ZonedDateTime) =
    toLocalDateTime() == other.toLocalDateTime()

fun Duration.ceilToMinutes() =
    ceil(seconds.toFloat() / ChronoUnit.MINUTES.duration.seconds).toInt()

fun Long.toZonedDateTime(): ZonedDateTime = ZonedDateTime.ofInstant(
    Instant.ofEpochMilli(this),
    ZoneId.systemDefault()
)

fun String.toZonedDateTime(): ZonedDateTime = ZonedDateTime.ofInstant(
    Instant.parse(this),
    ZoneId.systemDefault()
)

private fun DateTimeFormatter.formatWithLocale(temporal: TemporalAccessor) =
    withLocale(Locale.ENGLISH).format(temporal)
