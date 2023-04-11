package org.p2p.wallet.newsend.interactor

class SendFatalError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Throwable()
