package org.p2p.wallet.newsend.model

class SendFatalError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Throwable()
