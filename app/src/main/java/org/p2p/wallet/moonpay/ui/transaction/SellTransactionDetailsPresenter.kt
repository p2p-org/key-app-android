package org.p2p.wallet.moonpay.ui.transaction

import org.p2p.wallet.common.mvp.BasePresenter

class SellTransactionDetailsPresenter :
    BasePresenter<SellTransactionDetailsContract.View>(),
    SellTransactionDetailsContract.Presenter {
    override fun removeFromHistory() {
    }
}
