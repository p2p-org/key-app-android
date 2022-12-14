package org.p2p.wallet.sell.ui.payload

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.math.BigDecimal

interface SellPayloadContract {

    interface View : MvpView {
        fun showLoading(isVisible: Boolean)
        fun navigateToSellLock()
        fun showAvailableSolToSell(totalAmount: BigDecimal)
        fun setMinSolToSell(minAmount: BigDecimal, tokenSymbol: String)
        fun showErrorScreen()
        fun showNotEnoughMoney(minAmount: Double)
        fun updateValues(quoteAmount: Double, fee: Double)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun cashOut()
    }
}
