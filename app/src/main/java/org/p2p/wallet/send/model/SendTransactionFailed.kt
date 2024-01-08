package org.p2p.wallet.send.model

class SendTransactionFailed(
    transactionSignature: String,
    override val cause: Throwable
) : Throwable(message = "Failed to send transaction: signature=$transactionSignature")
