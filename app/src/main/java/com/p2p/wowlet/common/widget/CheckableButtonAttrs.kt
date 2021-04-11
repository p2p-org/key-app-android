package com.p2p.wowlet.common.widget

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class CheckableButtonAttrs(
    @DrawableRes
    val backgroundSelected: Int,
    @ColorRes
    val textColorSelected: Int,
    @DrawableRes
    val backgroundUnselected: Int,
    @ColorRes
    val textColorUnselected: Int
)