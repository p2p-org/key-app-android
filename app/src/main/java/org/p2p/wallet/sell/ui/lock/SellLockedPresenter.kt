package org.p2p.wallet.sell.ui.lock

import org.p2p.wallet.common.mvp.BasePresenter

class SellLockedPresenter :
    BasePresenter<SellLockedContract.View>(),
    SellLockedContract.Presenter {

    override fun removeFromHistory() {}

    override fun onSendClicked() {}

    override fun onRecipientClicked() {}

    override fun onCopyClicked() {}
}
