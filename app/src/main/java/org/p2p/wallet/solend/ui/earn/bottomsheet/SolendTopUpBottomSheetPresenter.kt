package org.p2p.wallet.solend.ui.earn.bottomsheet

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.model.SolendDepositToken

class SolendTopUpBottomSheetPresenter(
    private val deposit: SolendDepositToken,
) : BasePresenter<SolendTopUpBottomSheetContract.View>(),
    SolendTopUpBottomSheetContract.Presenter {

    override fun attach(view: SolendTopUpBottomSheetContract.View) {
        super.attach(view)
    }
}
