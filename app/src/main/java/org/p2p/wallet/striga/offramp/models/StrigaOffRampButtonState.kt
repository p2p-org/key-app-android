package org.p2p.wallet.striga.offramp.models

import org.p2p.wallet.R

sealed class StrigaOffRampButtonState(
    val titleResId: Int,
    val isEnabled: Boolean,
    val isLoading: Boolean = false,
    // button styles
    val iconDrawableResId: Int = R.drawable.ic_arrow_forward,
    val styleDisabledBgColorRes: Int = R.color.bg_rain,
    val styleDisabledTextColorRes: Int = R.color.mountain,
    val styleEnabledBgColorRes: Int = R.color.bg_night,
    val styleEnabledTextColorRes: Int = R.color.snow
) {

    val isAmountError: Boolean
        get() {
            return this is ErrorMinLimit || this is ErrorMaxLimit || this is ErrorInsufficientFunds
        }

    object LoadingRates : StrigaOffRampButtonState(
        titleResId = R.string.striga_off_ramp_button_getting_rates,
        isEnabled = false
    )

    object EnterAmount : StrigaOffRampButtonState(
        titleResId = R.string.striga_off_ramp_button_enter_amount,
        isEnabled = false
    )

    object ErrorGeneral : StrigaOffRampButtonState(
        titleResId = R.string.striga_off_ramp_button_error_general,
        isEnabled = false
    )

    object ErrorMinLimit : StrigaOffRampButtonState(
        titleResId = R.string.striga_off_ramp_button_error_min_limit,
        isEnabled = false
    )

    object ErrorMaxLimit : StrigaOffRampButtonState(
        titleResId = R.string.striga_off_ramp_button_error_max_limit,
        isEnabled = false
    )

    object ErrorInsufficientFunds : StrigaOffRampButtonState(
        titleResId = R.string.striga_off_ramp_button_error_insufficient_balance,
        isEnabled = false
    )

    object Enabled : StrigaOffRampButtonState(
        titleResId = R.string.striga_off_ramp_button_next,
        isEnabled = true
    )

    // todo: is not ready yet
    object NextProgress : StrigaOffRampButtonState(
        titleResId = R.string.striga_off_ramp_button_next,
        isEnabled = true,
        isLoading = true
    )

    override fun toString(): String {
        return this::class.simpleName ?: "Unknown"
    }
}
