package org.p2p.wallet.root

import org.p2p.wallet.transaction.model.ShowProgress

interface RootListener {
    fun showTransactionProgress(internalTransactionId: String, data: ShowProgress)
}
