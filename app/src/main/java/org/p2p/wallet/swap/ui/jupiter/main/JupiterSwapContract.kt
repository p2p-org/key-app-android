package org.p2p.wallet.swap.ui.jupiter.main

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel

interface JupiterSwapContract {
    interface View : MvpView {

        fun setFirstTokenWidgetState(state: SwapWidgetModel)
        fun setSecondTokenWidgetState(state: SwapWidgetModel)
        fun setButtonState(tokenA: SwapWidgetModel, tokenB: SwapWidgetModel)
        fun setRatioState(tokenA: SwapWidgetModel, tokenB: SwapWidgetModel)
    }

    interface Presenter : MvpPresenter<View>
}
