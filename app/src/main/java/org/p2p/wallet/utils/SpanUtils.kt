package org.p2p.wallet.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import org.p2p.wallet.R

object SpanUtils {

    fun highlightText(commonText: String, highlightedText: String, @ColorInt color: Int): SpannableString {
        val span = SpannableString(commonText)
        val startIndex = commonText.indexOf(highlightedText)
        val endIndex = startIndex + highlightedText.length

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
        val color = context.getColor(R.color.backgroundButtonPrimary)
        val outPutColoredText: Spannable = SpannableString(this)
        outPutColoredText.setSpan(ForegroundColorSpan(color), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val endIndex = length - 4
        outPutColoredText.setSpan(ForegroundColorSpan(color), endIndex, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return outPutColoredText
    }
}
