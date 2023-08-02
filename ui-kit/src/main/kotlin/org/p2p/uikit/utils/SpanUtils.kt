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

/**
 * @param text - text to highlight
 * @param color - color of the text (integer color, not a resource ID)
 * @param isUnderlined - underline text or not
 * @param onClick - action on click
 */
data class HighlightingOption(
    val text: String,
    @ColorInt val color: Int,
    val isUnderlined: Boolean = false,
    val onClick: (View) -> Unit = {},
)

object SpanUtils {

    fun highlightLinks(
        commonText: String,
        highlightedTexts: List<HighlightingOption>,
    ): SpannableString {
        val spannable = SpannableString(commonText)

        highlightedTexts.forEach { option ->
            var startIndex = commonText.indexOf(option.text)
            while (startIndex != -1) {
                val endIndex = startIndex + option.text.length

                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        widget.cancelPendingInputEvents()
                        option.onClick(widget)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = option.color
                        ds.isUnderlineText = option.isUnderlined
                    }
                }

                spannable.setSpan(
                    clickableSpan,
                    startIndex,
                    endIndex,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )

                startIndex = commonText.indexOf(option.text, startIndex + 1)
            }
        }

        return spannable
    }

    fun highlightLinkNoUnderline(
        commonText: String,
        highlightedText: String,
        @ColorInt color: Int,
        onClick: (View) -> Unit
    ): SpannableString {
        val spannable = SpannableString(commonText)
        val startIndex = commonText.indexOf(highlightedText)
        val endIndex = startIndex + highlightedText.length

        if (startIndex == -1) return spannable

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                widget.cancelPendingInputEvents()
                onClick(widget)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = color
                ds.isUnderlineText = false
            }
        }

        spannable.setSpan(
            clickableSpan,
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

            stringBuilder.setSpan(
                StyleSpan(Typeface.BOLD),
                copyStart,
                copyEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return stringBuilder
    }

    fun String.highlightPublicKey(context: Context): Spannable {
        val color = context.getColor(org.p2p.uikit.R.color.text_night)
        val outPutColoredText: Spannable = SpannableString(this)
        outPutColoredText.setSpan(ForegroundColorSpan(color), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val endIndex = length - 4
        outPutColoredText.setSpan(ForegroundColorSpan(color), endIndex, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return outPutColoredText
    }
}
