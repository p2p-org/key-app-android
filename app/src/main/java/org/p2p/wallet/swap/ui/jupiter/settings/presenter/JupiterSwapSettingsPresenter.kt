package org.p2p.wallet.swap.ui.jupiter.settings.presenter

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.ui.jupiter.settings.JupiterSwapSettingsContract

class JupiterSwapSettingsPresenter(
    private val stateManager: SwapStateManager
) : BasePresenter<JupiterSwapSettingsContract.View>(), JupiterSwapSettingsContract.Presenter {
}
