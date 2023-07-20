package org.p2p.wallet.striga.offramp.models

import androidx.annotation.StringRes
import org.p2p.core.utils.Constants
import org.p2p.wallet.R

enum class StrigaOffRampTokenType(
    @StringRes val titleResId: Int,
    val currencyName: String,
) {
    TokenA(
        R.string.striga_off_ramp_you_pay,
        Constants.USDC_SYMBOL,
    ),
    TokenB(
        R.string.striga_off_ramp_you_receive,
        Constants.EUR_SYMBOL,
    )
}
