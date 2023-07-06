package org.p2p.uikit.utils

import androidx.annotation.ColorRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import android.widget.TextView
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

fun TextView.setDrawableTint(@ColorRes colorRes: Int) {
    // if in XML layout you use android:startDrawable or android:endDrawable
    // you have to work with compoundDrawablesRelative array,
    // textView.compoundDrawables contains drawables
    // when they have been added with
    // android:leftDrawable or android:rightDrawable attributes
    val color = getColor(colorRes)
    (compoundDrawables + compoundDrawablesRelative)
        .filterNotNull()
        .forEach { it.setTint(color) }
}
