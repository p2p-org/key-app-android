package org.p2p.wallet.root

import org.p2p.wallet.transaction.model.NewShowProgress

interface RootListener {
    fun showTransactionProgress(internalTransactionId: String, data: NewShowProgress)
}
