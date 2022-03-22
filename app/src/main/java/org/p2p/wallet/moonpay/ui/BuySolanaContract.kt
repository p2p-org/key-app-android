package org.p2p.wallet.moonpay.ui

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.moonpay.model.BuyData

interface BuySolanaContract {

    interface View : MvpView {
        fun showTokenPrice(price: String)
        fun showData(data: BuyData)
        fun showLoading(isLoading: Boolean)
        fun showMessage(message: String?)
        fun navigateToMoonpay(amount: String)
        fun swapData(isSwapped: Boolean, prefixSuffixSymbol: String)
        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun setBuyAmount(amount: String, delayEnabled: Boolean = true)
        fun onContinueClicked()
        fun onSwapClicked()
        fun onBackPressed()
    }
}
