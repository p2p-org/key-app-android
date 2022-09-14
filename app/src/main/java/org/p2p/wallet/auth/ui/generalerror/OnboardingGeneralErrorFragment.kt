package org.p2p.wallet.auth.ui.generalerror

import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorContract.Presenter
import org.p2p.wallet.auth.ui.onboarding.NewOnboardingFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingGeneralErrorBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorContract.View as ContractView

private const val ARG_ERROR_TYPE = "ARG_ERROR_TYPE"

class OnboardingGeneralErrorFragment :
    BaseMvpFragment<ContractView, Presenter>(R.layout.fragment_onboarding_general_error),
    ContractView {

    companion object {
        fun create(error: GeneralErrorScreenError): OnboardingGeneralErrorFragment =
            OnboardingGeneralErrorFragment()
                .withArgs(ARG_ERROR_TYPE to error)
    }

    private val binding: FragmentOnboardingGeneralErrorBinding by viewBinding()

    private val screenError: GeneralErrorScreenError by args(ARG_ERROR_TYPE)

    override val presenter: Presenter by inject { parametersOf(screenError) }

    override val statusBarColor: Int = R.color.bg_lime
    override val navBarColor: Int = R.color.bg_night
    override val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.WHITE

    override fun updateText(title: String, message: String) {
        binding.textViewErrorTitle.text = title
        binding.textViewErrorSubtitle.text = message
    }

    override fun setViewState(errorState: GeneralErrorScreenError) = with(binding) {
        when (errorState) {
            is GeneralErrorScreenError.CriticalError -> {
                buttonReportBug.setOnClickListener {
                    IntercomService.showMessenger()
                }
                buttonToStartingScreen.setOnClickListener {
                    popAndReplaceFragment(NewOnboardingFragment.create(), inclusive = true)
                }

                buttonReportBug.setOnClickListener {
                    IntercomService.showMessenger()
                }
                buttonToStartingScreen.setOnClickListener {
                    popAndReplaceFragment(NewOnboardingFragment.create(), inclusive = true)
                }
                containerCommonButtons.isVisible = true
            }
            GeneralErrorScreenError.PhoneNumberDoesNotMatchError -> {
                errorState.titleResId?.let { textViewErrorTitle.text = getString(it) }
                errorState.messageResId?.let { textViewErrorSubtitle.text = getString(it) }

                buttonRestoreToStartingScreen.setOnClickListener {
                    popAndReplaceFragment(NewOnboardingFragment.create(), inclusive = true)
                }
                buttonRestoreWithPhone.setOnClickListener {
                    popAndReplaceFragment(PhoneNumberEnterFragment.create(), inclusive = true)
                }
                buttonRestoreByGoogle.setOnClickListener {
                    // TODO implement restore by google API
                }
                containerDeviceCustomShareButtons.isVisible = true
            }
        }
    }
}
