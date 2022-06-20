package org.p2p.wallet.history.interactor.stream

import org.p2p.wallet.history.model.RpcTransactionSignature

data class HistoryStreamItem(val account: String, val streamSource: RpcTransactionSignature?)
