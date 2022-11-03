package org.p2p.wallet.utils

import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

infix fun TextView.withTextOrGone(text: CharSequence?) {
    if (text.isNullOrEmpty() || text.contains("null")) {
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
