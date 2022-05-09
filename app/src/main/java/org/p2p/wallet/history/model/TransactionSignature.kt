package org.p2p.wallet.history.model

import org.p2p.solanaj.model.types.ConfirmationStatus

data class TransactionSignature(
    val signature: String,
    val status: ConfirmationStatus
)
