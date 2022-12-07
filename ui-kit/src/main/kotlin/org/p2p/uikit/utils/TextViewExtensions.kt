package org.p2p.uikit.utils

import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import org.p2p.core.utils.emptyString

infix fun TextView.withTextOrGone(text: CharSequence?) {
    if (text.isNullOrEmpty()) {
        isVisible = false
        this.text = emptyString()
    } else {
        isVisible = true
        this.text = text
    }
}

infix fun TextView.withTextOrInvisible(text: CharSequence?) {
    if (text.isNullOrEmpty()) {
        isInvisible = true
        this.text = emptyString()
    } else {
        isInvisible = false
        this.text = text
    }
}

fun TextView.setTextColorRes(@ColorRes color: Int) {
    setTextColor(context.getColor(color))
}
