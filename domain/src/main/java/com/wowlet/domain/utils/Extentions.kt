package com.wowlet.domain.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun Long.secondToDate(): String? {
    val dateFormat = SimpleDateFormat(
        "dd-MMM-yyyy @ hh:mm aa",
        Locale.getDefault()
    )
    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    return try {
        dateFormat.format(Date(this*1000L))
    } catch (e: ParseException) {
        null
    }
}