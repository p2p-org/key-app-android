package org.p2p.wallet.striga.offramp.models

import java.math.BigDecimal

sealed class StrigaOffRampTokenState(
    val amount: BigDecimal,
    val balance: BigDecimal = BigDecimal.ZERO
) {
    class Loading(balance: BigDecimal = BigDecimal.ZERO) : StrigaOffRampTokenState(
        amount = BigDecimal.ZERO,
        balance = balance
    )

    class LoadingBalance(amountA: BigDecimal) : StrigaOffRampTokenState(amountA, BigDecimal.ZERO)

    class Content(
        amount: BigDecimal,
        balance: BigDecimal = BigDecimal.ZERO
    ) : StrigaOffRampTokenState(amount, balance)

    class Disabled(balance: BigDecimal = BigDecimal.ZERO) : StrigaOffRampTokenState(
        amount = BigDecimal.ZERO,
        balance = balance
    )
}
