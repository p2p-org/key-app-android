package org.p2p.wallet.home.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

class Banner(
    @StringRes val optionTextId: Int,
    @StringRes val actionTextId: Int,
    @DrawableRes val drawableRes: Int,
    @ColorRes val backgroundColorRes: Int,
    val isSingle: Boolean = false
)
