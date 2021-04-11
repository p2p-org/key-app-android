package com.p2p.wowlet.domain.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun Long.secondToDate(): String? {
    val dateFormat = SimpleDateFormat(
        "dd-MMM-yyyy @ hh:mm aa",
        Locale.getDefault()
    )
    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    return try {
        dateFormat.format(Date(this * 1000L))
    } catch (e: ParseException) {
        null
    }
}

fun String.getActivityDate(): String {
    val parser = SimpleDateFormat("dd-MMM-yyyy '@' HH:mm a")
    val formatter = SimpleDateFormat("MMMM dd,yyyy '@' HH:mm a", Locale.ENGLISH)
    return formatter.format(parser.parse(this))
}

fun String.getTransactionDate(): String {
    val parser = SimpleDateFormat("dd-MMM-yyyy '@' HH:mm a")
    val formatter = SimpleDateFormat("dd MMM yyyy '@' HH:mm a", Locale.ENGLISH)
    return formatter.format(parser.parse(this))
}