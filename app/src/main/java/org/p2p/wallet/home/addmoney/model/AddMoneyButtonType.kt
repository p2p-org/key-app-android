package org.p2p.wallet.home.addmoney.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.R

enum class AddMoneyButtonType(
    @StringRes val titleResId: Int,
    @StringRes val subtitleRes: Int,
    @DrawableRes val iconResId: Int,
    @ColorRes val backgroundTintId: Int,
) {
    BANK_TRANSFER_MOONPAY(
        titleResId = R.string.bank_transfer_title,
        subtitleRes = R.string.bank_transfer_subtitle_one_percent_fees,
        iconResId = R.drawable.ic_bank_transfer,
        backgroundTintId = R.color.light_grass,
    ),
    BANK_TRANSFER_STRIGA(
        titleResId = R.string.bank_transfer_title,
        subtitleRes = R.string.bank_transfer_subtitle_zero_fees,
        iconResId = R.drawable.ic_bank_transfer,
        backgroundTintId = R.color.light_grass,
    ),
    BANK_CARD(
        titleResId = R.string.bank_card_title,
        subtitleRes = R.string.bank_card_subtitle,
        iconResId = R.drawable.ic_bank_card,
        backgroundTintId = R.color.light_sea,
    ),
    CRYPTO(
        titleResId = R.string.crypto_title,
        subtitleRes = R.string.crypto_subtitle,
        iconResId = R.drawable.ic_crypto,
        backgroundTintId = R.color.light_sun,
    )
}
