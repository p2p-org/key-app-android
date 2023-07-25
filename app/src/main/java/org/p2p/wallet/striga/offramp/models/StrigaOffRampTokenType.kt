package org.p2p.wallet.striga.offramp.models

import androidx.annotation.StringRes
import org.p2p.core.utils.Constants
import org.p2p.wallet.R

enum class StrigaOffRampTokenType(
    @StringRes val titleResId: Int,
    val currencyName: String,
) {
    TokenA(
        titleResId = R.string.striga_off_ramp_you_pay,
        currencyName = Constants.USDC_SYMBOL,
    ),
    TokenB(
        titleResId = R.string.striga_off_ramp_you_receive,
        currencyName = Constants.EUR_SYMBOL,
    )
}
