package org.p2p.core.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.URLSpan

private const val USERNAME_KEY_APP_DOMAIN = ".key"

fun emptyString() = ""

fun StringBuilder.appendBreakLine() {
    append("\n")
}

fun Spanned.removeLinksUnderline(): SpannableString {
    val spannable = SpannableString(this)
    for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
        spannable.setSpan(
            object : URLSpan(u.url) {
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            },
            spannable.getSpanStart(u),
            spannable.getSpanEnd(u), 0
        )
    }
    return spannable
}

fun formatUsername(username: String): String {
    return if (username.endsWith(USERNAME_KEY_APP_DOMAIN)) {
        "@$username"
    } else {
        username
    }
}
