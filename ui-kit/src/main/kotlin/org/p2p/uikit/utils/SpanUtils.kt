package org.p2p.uikit.utils

import androidx.annotation.ColorInt
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import org.p2p.core.utils.emptyString
import org.p2p.uikit.R

object SpanUtils {

    fun highlightLinkNoUnderline(
        text: String,
        highlighted: String,
        @ColorInt color: Int,
        onClick: (View) -> Unit
    ): SpannableString {
        val spannable = SpannableString(text)
        val startIndex = text.indexOf(highlighted)
        val endIndex = startIndex + highlighted.length

        if (startIndex == -1) return spannable

        spannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClick(widget)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = color
                    ds.isUnderlineText = false
                }
            },
            startIndex,
            endIndex,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        return spannable
    }

    fun highlightText(commonText: String, highlightedText: String, @ColorInt color: Int): SpannableString {
        if (commonText.isEmpty()) return SpannableString(emptyString())
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
        val color = context.getColor(R.color.text_night)
        val outPutColoredText: Spannable = SpannableString(this)
        outPutColoredText.setSpan(ForegroundColorSpan(color), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val endIndex = length - 4
        outPutColoredText.setSpan(ForegroundColorSpan(color), endIndex, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return outPutColoredText
    }
}
