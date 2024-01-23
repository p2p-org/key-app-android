package org.p2p.uikit.components

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import org.p2p.core.token.Token
import org.p2p.uikit.utils.text.TextViewCellModel

interface UiKitSendDetailsWidgetContract {
    fun showToken(token: Token.Active)
    fun showAroundValue(value: String)
    fun showSliderCompleteAnimation()
    fun showFeeViewLoading(isLoading: Boolean)
    fun showDelayedFeeViewLoading(isLoading: Boolean)

    fun setSwitchLabel(symbol: String)
    fun setInputColor(@ColorRes colorRes: Int)
    fun setMainAmountLabel(symbol: String)
    fun setMaxButtonVisible(isVisible: Boolean)
    fun setFeeLabel(text: String)
    fun setFeeLabelRes(@StringRes textRes: Int)
    fun showBottomFeeValue(fee: TextViewCellModel)
    fun setFeeColor(@ColorRes colorRes: Int)
    fun setTotalValue(text: String)
    fun setTokenContainerEnabled(isEnabled: Boolean)
    fun setInputEnabled(isEnabled: Boolean)
    fun showFeeViewVisible(isVisible: Boolean)

    fun restoreSlider()
}
