package org.p2p.wallet.history.ui.historylist

import org.p2p.core.utils.Constants
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

sealed class HistoryListViewType(val mintAddress: Base58String) {
    object AllHistory : HistoryListViewType(Constants.WRAPPED_SOL_MINT.toBase58Instance())
    class HistoryForToken(mintAddress: Base58String) : HistoryListViewType(mintAddress)
}
