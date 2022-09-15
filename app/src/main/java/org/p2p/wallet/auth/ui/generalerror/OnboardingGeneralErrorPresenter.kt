package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter

class OnboardingGeneralErrorPresenter(
    private val error: GeneralErrorScreenError,
    private val resourcesProvider: ResourcesProvider,
) : BasePresenter<OnboardingGeneralErrorContract.View>(),
    OnboardingGeneralErrorContract.Presenter {

    override fun attach(view: OnboardingGeneralErrorContract.View) {
        super.attach(view)

        when (error) {
            is GeneralErrorScreenError.CriticalError -> {
                val title = resourcesProvider.getString(
                    R.string.onboarding_general_error_critical_error_title
                )
                val subTitle = resourcesProvider.getString(
                    R.string.onboarding_general_error_critical_error_sub_title,
                    error.errorCode
                )
                view.updateText(title, subTitle)
                view.setViewState(error)
            }
            else -> {
                view.setViewState(error)
            }
        }
    }
}
