package org.p2p.wallet.swap.ui.jupiter.main

import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel

interface JupiterSwapContract {
    interface View : MvpView {
        fun setFirstTokenWidgetState(state: SwapWidgetModel)
        fun setSecondTokenWidgetState(state: SwapWidgetModel)
        fun setButtonState(buttonState: SwapButtonState)
        fun setRatioState(state: TextViewCellModel)
    }

    interface Presenter : MvpPresenter<View> {
        fun switchTokens()
        fun onTokenAmountChange(amount: String)
        fun onSwapTokenClick()
        fun finishFeature(stateManagerHolderKey: String)
    }
}
