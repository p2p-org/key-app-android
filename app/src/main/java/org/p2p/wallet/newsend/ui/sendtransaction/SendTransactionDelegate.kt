package org.p2p.wallet.newsend.ui.sendtransaction

import kotlinx.coroutines.CoroutineScope
import org.p2p.wallet.newsend.ui.NewSendContract

interface SendTransactionDelegate {

    fun send()
    fun attach(presenterScope: CoroutineScope, view: NewSendContract.View)
    fun detach()
}
