package org.p2p.wallet.swap.ui.settings

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor

class SwapSettingsPresenter(
    private val orcaSwapInteractor: OrcaSwapInteractor
) : BasePresenter<SwapSettingsContract.View>(), SwapSettingsContract.Presenter {

    override fun setFeePayToken(token: Token.Active) {
        orcaSwapInteractor.setFeePayToken(token)
    }
}