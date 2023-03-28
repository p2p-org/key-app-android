package org.p2p.wallet.bridge.send.ui

import org.p2p.core.token.Token
import org.p2p.uikit.components.UiKitSendDetailsWidget
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.uikit.components.UiKitSliderSolidButton

class BridgeSendWidgetDelegate(
    private val widget: UiKitSendDetailsWidget,
    private val slider: UiKitSliderSolidButton,
    private val presenter: BridgeSendContract.Presenter
) : UiKitSendDetailsWidgetContract {

    override fun showToken(token: Token.Active) {
        widget.setToken(token)
    }

    override fun showAroundValue(value: String) {
        widget.setAroundValue(value)
    }

    override fun showSliderCompleteAnimation() {
        slider.showCompleteAnimation()
    }

    override fun showFeeViewLoading(isLoading: Boolean) {
        widget.showFeeLoading(isLoading)
    }

    override fun showDelayedFeeViewLoading(isLoading: Boolean) {
        widget.showDelayedFeeViewLoading(isLoading)
    }

    override fun setSwitchLabel(symbol: String) {
        widget.setSwitchLabel(symbol)
    }

    override fun setInputColor(colorRes: Int) {
        widget.setInputTextColor(colorRes)
    }

    override fun setMainAmountLabel(symbol: String) {
        widget.setMainAmountLabel(symbol)
    }

    override fun setMaxButtonVisible(isVisible: Boolean) {
        widget.setMaxButtonVisible(isVisible)
    }

    override fun setFeeLabel(text: String) {
        widget.setFeeLabel(text)
    }

    override fun setTokenContainerEnabled(isEnabled: Boolean) {
        widget.setTokenContainerEnabled(isEnabled)
    }

    override fun setInputEnabled(isEnabled: Boolean) {
        widget.setInputEnabled(isEnabled)
    }

    override fun showFeeViewVisible(isVisible: Boolean) {
        widget.showFeeVisible(isVisible)
    }

    override fun restoreSlider() {
        slider.restoreSlider()
    }
}
