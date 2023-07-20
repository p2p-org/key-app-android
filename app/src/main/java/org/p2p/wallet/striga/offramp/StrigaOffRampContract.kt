package org.p2p.wallet.striga.offramp

import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.striga.offramp.models.StrigaOffRampButtonState

interface StrigaOffRampContract {

    interface View : MvpView {
        fun setTokenAWidgetState(state: SwapWidgetModel)
        fun setTokenBWidgetState(state: SwapWidgetModel)
        fun setRatioState(state: TextViewCellModel?)
        fun setButtonState(buttonState: StrigaOffRampButtonState)
        fun setTokenAErrorState(isError: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun onTokenAAmountChange(amountA: String)
        fun onTokenBAmountChange(amountB: String)
        fun onAllAmountClick()
        fun onSubmit()
    }
}
