package org.p2p.wallet.auth.ui.generalerror

import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.components.UiKitButton
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.RestoreFailureState
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorContract.Presenter
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingGeneralErrorBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorContract.View as ContractView

private const val ARG_ERROR_STATE = "ARG_ERROR_STATE"

class OnboardingGeneralErrorFragment :
    BaseMvpFragment<ContractView, Presenter>(R.layout.fragment_onboarding_general_error),
    ContractView,
    GoogleSignInHelper.GoogleSignInErrorHandler {

    companion object {

        fun create(handledState: RestoreFailureState.TitleSubtitleError) =
            OnboardingGeneralErrorFragment()
                .withArgs(ARG_ERROR_STATE to handledState)
    }

    private val binding: FragmentOnboardingGeneralErrorBinding by viewBinding()

    private val screenError: RestoreFailureState.TitleSubtitleError by args(ARG_ERROR_STATE)

    override val presenter: Presenter by inject { parametersOf(screenError) }

    private val signInHelper: GoogleSignInHelper by inject()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

    override val statusBarColor: Int = R.color.bg_lime
    override val navBarColor: Int = R.color.bg_night
    override val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.WHITE

    override fun startGoogleFlow() {
        signInHelper.showSignInDialog(requireContext(), googleSignInLauncher)
    }

    private fun handleSignResult(result: ActivityResult) {
        signInHelper.parseSignInResult(requireContext(), result, errorHandler = this)?.let { credential ->
            setRestoreByGoogleLoadingState(isRestoringByGoogle = true)
            presenter.setGoogleIdToken(credential.id, credential.googleIdToken.orEmpty())
        }
    }

    override fun setRestoreByGoogleLoadingState(isRestoringByGoogle: Boolean) {
        with(binding) {
            buttonRestoreByGoogle.apply {
                isLoadingState = isRestoringByGoogle
                isEnabled = !isRestoringByGoogle
                isVisible = true
            }
            buttonPrimaryFirst.isEnabled = !isRestoringByGoogle
        }
    }

    override fun onConnectionError() {
        setRestoreByGoogleLoadingState(isRestoringByGoogle = false)
        showUiKitSnackBar(message = getString(R.string.error_general_message))
    }

    override fun onCommonError() {
        setRestoreByGoogleLoadingState(isRestoringByGoogle = false)
        showUiKitSnackBar(messageResId = R.string.onboarding_google_services_error)
    }

    override fun navigateToPinCreate() {
        popAndReplaceFragment(NewCreatePinFragment.create(), inclusive = true)
    }

    override fun navigateToEnterPhone() {
        popAndReplaceFragment(PhoneNumberEnterFragment.create(), inclusive = true)
    }

    override fun showState(state: RestoreFailureState.TitleSubtitleError): Unit = with(binding) {
        textViewErrorTitle.text = state.title
        textViewErrorSubtitle.text = state.subtitle
        state.email?.let {
            textViewErrorEmail.text = it
            textViewErrorEmail.isVisible = true
        }
        state.googleButton?.let { buttonState ->
            buttonRestoreByGoogle.setText(buttonState.titleResId)
            buttonState.iconResId?.let { buttonRestoreByGoogle.setIconResource(it) }
            buttonState.iconTintResId?.let { buttonRestoreByGoogle.setIconTintResource(it) }
            setButtonAction(buttonRestoreByGoogle, buttonState.buttonAction)
            buttonRestoreByGoogle.isVisible = true
        }
        state.primaryFirstButton?.let { buttonState ->
            buttonPrimaryFirst.setText(buttonState.titleResId)
            setButtonAction(buttonPrimaryFirst, buttonState.buttonAction)
            buttonPrimaryFirst.isVisible = true
        }
        state.secondaryFirstButton?.let { buttonState ->
            buttonSecondaryFirst.setText(buttonState.titleResId)
            setButtonAction(buttonSecondaryFirst, buttonState.buttonAction)
            buttonSecondaryFirst.isVisible = true
        }
    }

    private fun setButtonAction(button: UiKitButton, action: RestoreFailureState.TitleSubtitleError.ButtonAction) {
        button.setOnClickListener {
            when (action) {
                RestoreFailureState.TitleSubtitleError.ButtonAction.OPEN_INTERCOM -> {
                    IntercomService.showMessenger()
                }
                RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_GOOGLE_AUTH -> {
                    presenter.useGoogleAccount()
                }
                RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_ENTER_PHONE -> popAndReplaceFragment(
                    PhoneNumberEnterFragment.create(),
                    inclusive = true
                )
                RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_START_SCREEN -> popAndReplaceFragment(
                    OnboardingRootFragment.create(),
                    inclusive = true
                )
            }
        }
    }
}
