package org.p2p.wallet.send.model

class SendFatalError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Throwable()
