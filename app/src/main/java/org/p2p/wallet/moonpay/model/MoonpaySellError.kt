package org.p2p.wallet.moonpay.model

sealed class MoonpaySellError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Throwable() {
    class UnauthorizedRequest(cause: Throwable) : MoonpaySellError(message = "Request was unauthorized", cause = cause)
}
