package org.p2p.uikit.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt

object SpanUtils {

    fun highlightText(commonText: String, highlightedText: String, @ColorInt color: Int): SpannableString {
        val span = SpannableString(commonText)
        val startIndex = commonText.indexOf(highlightedText).coerceAtLeast(0)
        val endIndex = (startIndex + highlightedText.length).coerceAtMost(commonText.length)

        if (startIndex == -1) return span

        span.setSpan(ForegroundColorSpan(color), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return span
    }

    fun setTextBold(commonText: String, vararg boldText: String): SpannableString {
        val stringBuilder = SpannableString(commonText)

        boldText.forEach {
            val copyStart = stringBuilder.indexOf(it)
            val copyEnd = stringBuilder.indexOf(it) + it.length
            if (copyStart == -1) return@forEach

            stringBuilder.setSpan(StyleSpan(Typeface.BOLD), copyStart, copyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return stringBuilder
    }

    fun String.highlightPublicKey(context: Context): Spannable {
        // FIXME: This color is backgroundButtonPrimary from the old design system
        // FIXME: Delete/update when redesign is finished
        val color = Color.parseColor("#5887FF")
        val outPutColoredText: Spannable = SpannableString(this)
        outPutColoredText.setSpan(ForegroundColorSpan(color), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val endIndex = length - 4
        outPutColoredText.setSpan(ForegroundColorSpan(color), endIndex, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return outPutColoredText
    }
}
