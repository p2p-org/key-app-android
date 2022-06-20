package org.p2p.wallet.moonpay.ui

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.moonpay.model.BuyViewData

interface BuySolanaContract {

    interface View : MvpView {
        fun showTokenPrice(price: String)
        fun showData(viewData: BuyViewData)
        fun showLoading(isLoading: Boolean)
        fun showMessage(message: String?)
        fun setContinueButtonEnabled(isEnabled: Boolean)
        fun navigateToMoonpay(amount: String)
        fun swapData(isSwapped: Boolean, prefixSuffixSymbol: String)
        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun setBuyAmount(amount: String, isDelayEnabled: Boolean = true)
        fun onContinueClicked()
        fun onSwapClicked()
        fun onBackPressed()
    }
}
