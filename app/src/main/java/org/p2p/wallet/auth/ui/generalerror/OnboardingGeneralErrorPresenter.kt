package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.common.mvp.BasePresenter

class OnboardingGeneralErrorPresenter(
    private val gatewayHandledState: GatewayHandledState
) : BasePresenter<OnboardingGeneralErrorContract.View>(),
    OnboardingGeneralErrorContract.Presenter {

    override fun attach(view: OnboardingGeneralErrorContract.View) {
        super.attach(view)
        when (gatewayHandledState) {
            is GatewayHandledState.TitleSubtitleError -> {
                view.setState(gatewayHandledState)
            }
            is GatewayHandledState.CriticalError -> {
                view.setState(gatewayHandledState)
            }
        }
    }
}
