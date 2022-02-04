package org.p2p.wallet.settings.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class SettingItem(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @DrawableRes val iconRes: Int,
    var onItemClickListener: ((SettingItem) -> Unit)? = null
)