package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter

class OnboardingGeneralErrorPresenter(
    private val errorType: GeneralErrorScreenErrorType
) : BasePresenter<OnboardingGeneralErrorContract.View>(),
    OnboardingGeneralErrorContract.Presenter {

    override fun attach(view: OnboardingGeneralErrorContract.View) {
        super.attach(view)
        val titleRes: Int
        val subTitleRes: Int

        when (errorType) {
            GeneralErrorScreenErrorType.CRITICAL_ERROR -> {
                titleRes = R.string.onboarding_general_error_critical_error_title
                subTitleRes = R.string.onboarding_general_error_critical_error_sub_title
            }
        }
        view.updateTitle(titleRes = titleRes)
        view.updateSubtitle(subTitleRes = subTitleRes)
    }
}
