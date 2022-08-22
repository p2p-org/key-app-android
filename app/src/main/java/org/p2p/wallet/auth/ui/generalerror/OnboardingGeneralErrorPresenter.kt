package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter

class OnboardingGeneralErrorPresenter(
    private val error: GeneralErrorScreenError,
    private val resourcesProvider: ResourcesProvider
) : BasePresenter<OnboardingGeneralErrorContract.View>(),
    OnboardingGeneralErrorContract.Presenter {

    override fun attach(view: OnboardingGeneralErrorContract.View) {
        super.attach(view)
        val title: String
        val subTitle: String

        when (error) {
            is GeneralErrorScreenError.CriticalError -> {
                title = resourcesProvider.getString(
                    R.string.onboarding_general_error_critical_error_title
                )
                subTitle = resourcesProvider.getString(
                    R.string.onboarding_general_error_critical_error_sub_title,
                    error.errorCode
                )
            }
        }
        view.updateTitle(title)
        view.updateSubtitle(subTitle)
    }
}
