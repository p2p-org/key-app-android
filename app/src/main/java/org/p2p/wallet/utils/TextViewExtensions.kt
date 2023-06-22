package org.p2p.wallet.utils

import androidx.annotation.StringRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import android.widget.TextView

infix fun TextView.withTextOrGone(text: CharSequence?) {
    if (text.isNullOrEmpty() || text.contains("null")) {
        isVisible = false
        this.text = emptyString()
    } else {
        isVisible = true
        this.text = text
    }
}

infix fun TextView.withTextResOrGone(@StringRes text: Int?) {
    if (text == null) {
        isVisible = false
        this.text = emptyString()
    } else {
        isVisible = true
        this.setText(text)
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
