package org.p2p.wallet.jupiter.ui.main

import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.jupiter.statemanager.price_impact.SwapPriceImpactView
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.transaction.ui.SwapTransactionBottomSheetData

interface JupiterSwapContract {
    interface View : MvpView {
        fun setFirstTokenWidgetState(state: SwapWidgetModel)
        fun setSecondTokenWidgetState(state: SwapWidgetModel)
        fun setButtonState(buttonState: SwapButtonState)
        fun setRatioState(state: TextViewCellModel?)
        fun showSolErrorToast()
        fun closeScreen()
        fun openChangeTokenAScreen()
        fun openChangeTokenBScreen()
        fun showPriceImpact(priceImpact: SwapPriceImpactView)
        fun scrollToPriceImpact()
        fun showProgressDialog(internalTransactionId: String, transactionDetails: SwapTransactionBottomSheetData)
        fun showDefaultSlider()
        fun showFullScreenError()
        fun hideFullScreenError()
        fun showDebugInfo(debugInfo: TextViewCellModel)
        fun showKeyboard()
    }

    interface Presenter : MvpPresenter<View> {
        fun switchTokens()
        fun onTokenAmountChange(amount: String)
        fun onSwapSliderClicked()
        fun onAllAmountClick()
        fun onChangeTokenAClick()
        fun onChangeTokenBClick()
        fun onBackPressed()
        fun finishFeature(stateManagerHolderKey: String)
        fun reloadFeature()
        fun changeSlippage(newSlippageValue: Slippage)
        fun onTryAgainClick()
        fun pauseStateManager()
        fun resumeStateManager()
    }
}
