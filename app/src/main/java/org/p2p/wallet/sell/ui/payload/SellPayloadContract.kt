package org.p2p.wallet.sell.ui.payload

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal

interface SellPayloadContract {

    interface View : MvpView {
        fun showLoading(isVisible: Boolean)
        fun showAvailableSolToSell(totalAmount: BigDecimal)
        fun setMinSolToSell(minAmount: BigDecimal, tokenSymbol: String)
        fun showMoonpayWidget(url: String)
        fun navigateToSellLock(solAmount: BigDecimal, usdAmount: String, moonpayAddress: Base58String)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun cashOut()
    }
}
