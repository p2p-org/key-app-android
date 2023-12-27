package org.p2p.wallet.transaction.model.progressstate

import org.p2p.core.utils.emptyString

sealed class StrigaOffRampTransactionState : TransactionState() {
    object UsdcWithdrawSuccess : Success()
    object UsdcWithdrawError : Error(emptyString())
    object EurWithdrawSuccess : Success()
}
