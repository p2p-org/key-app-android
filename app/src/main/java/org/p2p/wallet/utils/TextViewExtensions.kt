package org.p2p.wallet.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan

fun String.setTextBold(vararg boldText: String): SpannableString {
    val stringBuilder = SpannableString(this)

    boldText.forEach {
        val copyStart = stringBuilder.indexOf(it)
        val copyEnd = stringBuilder.indexOf(it) + it.length
        if (copyStart == -1 || copyEnd == -1) return@forEach

        stringBuilder.setSpan(StyleSpan(Typeface.BOLD), copyStart, copyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    return stringBuilder
}