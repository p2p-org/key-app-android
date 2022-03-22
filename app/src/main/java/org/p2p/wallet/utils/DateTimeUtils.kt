package org.p2p.wallet.utils

import android.content.Context
import android.os.Build
import org.p2p.wallet.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object DateTimeUtils {

    private val formatsCache = HashMap<FormatterKey, SimpleDateFormat>()

    private const val PATTERN_FULL_DAY = "HH:mm"
    private const val PATTERN_AM_PM = "hh:mm a"
    private const val PATTERN_DATE = "dd.MM.yyyy"
    private const val PATTERN_CURRENT_YEAR_DATE = "d MMM"
    private const val PATTERN_CURRENT_YEAR_DATE_FULL = "d MMMM"
    private const val PATTERN_DAY_OF_WEEK_SHORT = "EE"
    private const val PATTERN_DAY_OF_WEEK_FULL = "EEEE"
    private const val PATTERN_DATE_AND_TIME = "dd.MM HH:mm"
    private const val PATTERN_DATEPICKER_DATE = "EE, dd MMM"

    private const val SECOND = 1000
    private const val MINUTE = 60 * SECOND
    private const val HOUR = 60 * MINUTE
    const val DAY = 24 * HOUR
    private const val WEEK = 7 * DAY
    private const val DAYS_IN_WEEK = 7
    private const val DAYS_IN_YEAR = 365

    private fun getFormatter(pattern: String, context: Context? = null): SimpleDateFormat {
        val locale = when {
            context == null -> Locale.getDefault()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> context.resources.configuration.locales[0]
            else -> @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        val formatterKey = FormatterKey(locale, pattern)
        return formatsCache[formatterKey] ?: run {
            val newVal = SimpleDateFormat(pattern, locale)
            formatsCache[formatterKey] = newVal
            newVal
        }
    }

    fun getDateFormatted(datetime: Long, context: Context): String {
        val currentTime = System.currentTimeMillis()

        val targetDate = convertToMidnight(datetime)
        val currentDate = convertToMidnight(currentTime)
        val daysDiff = getDateDiff(targetDate, currentDate, TimeUnit.DAYS)
        return when {
            datetime == 0L -> ""
            daysDiff == 0L -> convertTo12or24Format(datetime, context)
            daysDiff == 1L -> context.getString(R.string.common_yesterday).lowercase(Locale.ENGLISH)

            daysDiff < DAYS_IN_WEEK -> getFormatter(PATTERN_DAY_OF_WEEK_SHORT, context)
                .format(datetime)
                .lowercase(Locale.ENGLISH)

            daysDiff < DAYS_IN_YEAR -> getFormatter(PATTERN_CURRENT_YEAR_DATE, context).format(datetime)
            else -> getFormatter(PATTERN_DATE, context).format(datetime)
        }
    }

    fun getDateForDatePicker(datetime: Long, context: Context): String {
        return getFormatter(PATTERN_DATEPICKER_DATE, context).format(datetime)
    }

    fun getFormattedDate(timestamp: Long): String = getFormatter(PATTERN_DATE).format(Date(timestamp))

    fun getFormattedDateAndTime(timestamp: Long): String = getFormatter(PATTERN_DATE_AND_TIME)
        .format(timestamp)

    private fun getCalendar(timeInMillis: Long): Calendar {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = timeInMillis
        return cal
    }

    private fun convertToMidnight(timeInMillis: Long): Long {
        val calendar = getCalendar(timeInMillis)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getDateDiff(left: Long, right: Long, timeUnit: TimeUnit): Long =
        timeUnit.convert(right - left, TimeUnit.MILLISECONDS)

    private fun getNumberOfDaysInMonth(timeInMillis: Long): Int {
        val calendar = getCalendar(timeInMillis)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun convertTo12or24Format(time: Long, context: Context): String {
        val is24HourFormat = android.text.format.DateFormat.is24HourFormat(context)

        return if (is24HourFormat) {
            getFormatter(PATTERN_FULL_DAY, context).format(time)
        } else {
            getFormatter(PATTERN_AM_PM, context).format(time)
        }
    }

    fun millisToMMSS(millis: Long): String {
        val seconds = (millis / 1000.0).roundToInt()
        val s = seconds % 60
        val m = seconds / 60
        return String.format(Locale.US, "%02d:%02d", m, s)
    }

    fun millisToHHMMSS(millis: Long): String {
        val seconds = (millis / 1000.0).roundToInt()
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / 3600
        return when {
            h > 0 -> String.format("%02d:%02d:%02d", h, m, s)
            else -> String.format("%02d:%02d", m, s)
        }
    }

    fun secondsToMMSS(seconds: Long): String {
        val s = seconds % 60
        val m = seconds / 60
        return String.format(Locale.US, "%02d:%02d", m, s)
    }

    data class FormatterKey(val locale: Locale, val pattern: String)
}
