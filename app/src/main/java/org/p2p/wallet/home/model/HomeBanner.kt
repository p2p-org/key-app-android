package org.p2p.wallet.home.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes

class HomeBanner(
    @IdRes val id: Int,
    @StringRes val titleTextId: Int,
    @StringRes val subtitleTextId: Int,
    @StringRes val buttonTextId: Int,
    @DrawableRes val drawableRes: Int,
    @ColorRes val backgroundColorRes: Int
)
