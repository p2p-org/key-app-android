package org.p2p.uikit.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment

@ColorInt
fun Context.colorFromTheme(@AttrRes attrId: Int): Int {
    if (attrId == 0) return 0
    val typedValue = TypedValue()
    theme.resolveAttribute(attrId, typedValue, true)
    val colorInt = typedValue.data
    return Color.rgb(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
}

@ColorInt
fun View.colorFromTheme(@AttrRes attrId: Int): Int {
    if (attrId == 0) return 0
    var c = context as? ContextWrapper
    while (c != null) {
        if (c is Activity) return c.colorFromTheme(attrId)
        c = c.baseContext as? ContextWrapper
    }
    return 0
}

@ColorInt
fun Fragment.colorFromTheme(@AttrRes attrId: Int): Int = requireActivity().colorFromTheme(attrId)

fun Activity.resFromTheme(@AttrRes attrId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrId, typedValue, true)
    return typedValue.resourceId
}

fun View.resFromTheme(@AttrRes attrId: Int): Int {
    var c = context as? ContextWrapper
    while (c != null) {
        if (c is Activity) return c.resFromTheme(attrId)
        c = c.baseContext as? ContextWrapper
    }
    return 0
}

fun Fragment.resFromTheme(@AttrRes attrId: Int): Int = requireActivity().resFromTheme(attrId)
