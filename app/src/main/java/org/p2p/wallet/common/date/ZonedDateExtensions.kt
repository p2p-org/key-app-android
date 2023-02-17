package org.p2p.wallet.common.date

import android.content.Context
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalAccessor
import timber.log.Timber
import java.util.Locale
import kotlin.math.ceil
import org.p2p.wallet.R

private val monthDayFormatter = DateTimeFormatter.ofPattern("MMMM dd")
private val monthDayYearFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd.yyyy HH:mm")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun ZonedDateTime.toDateString(context: Context): String {
    val day = withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
    val today = Today.value
    val yesterday = today.minusDays(1)
    return when {
        day.isEqual(today) -> context.getString(R.string.common_today)
        day.isEqual(yesterday) -> context.getString(R.string.common_yesterday)
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

fun String.toZonedDateTime(): ZonedDateTime {
    return try {
        ZonedDateTime.ofInstant(Instant.parse(this), ZoneId.systemDefault())
    } catch (e: Exception) {
        Timber.e(e, "Failed to parses string in UTC format, such as '2022-12-01T10:15:20Z'")
        ZonedDateTime.now()
    }
}

private fun DateTimeFormatter.formatWithLocale(temporal: TemporalAccessor) =
    withLocale(Locale.ENGLISH).format(temporal)
