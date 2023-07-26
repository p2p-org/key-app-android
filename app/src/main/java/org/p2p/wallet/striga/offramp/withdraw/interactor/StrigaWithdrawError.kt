package org.p2p.wallet.striga.offramp.withdraw.interactor

sealed class StrigaWithdrawError(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable(message, cause) {
    class StrigaSendUsdcFailed(message: String, cause: Throwable? = null) :
        StrigaWithdrawError(message, cause)

    class StrigaSendEurFailed(message: String, cause: Throwable? = null) :
        StrigaWithdrawError(message, cause)
}
