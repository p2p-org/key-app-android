package org.p2p.wallet.history.ui.historylist

import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.Constants

sealed class HistoryListViewType(val mintAddress: Base58String) {
    object AllHistory : HistoryListViewType(Constants.WRAPPED_SOL_MINT.toBase58Instance())
    class HistoryForToken(mintAddress: Base58String) : HistoryListViewType(mintAddress)
}
