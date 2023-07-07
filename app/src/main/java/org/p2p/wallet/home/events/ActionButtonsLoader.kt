package org.p2p.wallet.home.events

import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.ui.main.HomeInteractor

class ActionButtonsLoader(
    private val homeInteractor: HomeInteractor,
    private val sellEnabledFeatureToggle: EthAddressEnabledFeatureToggle
) : AppLoader {

    override suspend fun onLoad() {
        val isSellFeatureToggleEnabled = sellEnabledFeatureToggle.isFeatureEnabled
        val isSellAvailable = homeInteractor.isSellFeatureAvailable()

        val buttons = mutableListOf(ActionButton.TOP_UP_BUTTON)

        if (isSellAvailable) {
            buttons += ActionButton.SELL_BUTTON
        }

        buttons += ActionButton.SEND_BUTTON

        if (!isSellFeatureToggleEnabled) {
            buttons += ActionButton.SWAP_BUTTON
        }
        homeInteractor.updateHomeActionButtons(buttons)
    }

    override suspend fun onRefresh() {
        onLoad()
    }
}
