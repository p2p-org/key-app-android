package org.p2p.wallet.home.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

class HomeBannerItem(
    @StringRes val titleTextId: Int,
    @StringRes val subtitleTextId: Int,
    @StringRes val buttonTextId: Int,
    @DrawableRes val drawableRes: Int,
    @ColorRes val backgroundColorRes: Int
)
