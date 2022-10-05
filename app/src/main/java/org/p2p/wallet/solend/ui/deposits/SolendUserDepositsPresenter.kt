package org.p2p.wallet.solend.ui.deposits

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.model.SolendDepositToken

class SolendUserDepositsPresenter() :
    BasePresenter<SolendUserDepositsContract.View>(),
    SolendUserDepositsContract.Presenter {

    override fun onAddMoreClicked(token: SolendDepositToken) {
        // TODO PWN-5020 make real impl
    }

    override fun onWithdrawClicked(token: SolendDepositToken) {
        // TODO PWN-5020 make real impl
    }
}
