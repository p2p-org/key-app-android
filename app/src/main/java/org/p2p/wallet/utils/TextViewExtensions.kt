package org.p2p.wallet.utils

import android.widget.TextView
import androidx.core.view.isVisible

infix fun TextView.withTextOrGone(text: CharSequence?) {
    if (text.isNullOrEmpty()) {
        isVisible = false
        this.text = ""
    } else {
        isVisible = true
        this.text = text
    }
}