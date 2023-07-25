package org.p2p.wallet.striga.offramp.withdraw

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize

sealed interface StrigaWithdrawFragmentType : Parcelable {
    /**
     * First step of off-ramp - entering the IBAN and BIC numbers and sending usdc to
     * striga user blockchain address
     */
    @Parcelize
    data class ConfirmUsdcOffRamp(
        val amountInUsdc: BigDecimal,
    ) : StrigaWithdrawFragmentType

    /**
     * Second step of off-ramp - if there no IBAN/BIC available we need manually enter it
     */
    @Parcelize
    object ConfirmEurOffRamp : StrigaWithdrawFragmentType
}
