package org.p2p.uikit.utils.drawable

import androidx.annotation.ColorInt
import android.graphics.drawable.GradientDrawable

fun buildGradientDrawableRectangle(
    cornerRadius: Float,
    strokeWidth: Int,
    @ColorInt strokeColor: Int,
    @ColorInt backgroundColor: Int
): GradientDrawable = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    this.cornerRadius = cornerRadius
    setColor(backgroundColor)
    setStroke(strokeWidth, strokeColor)
}
