package org.p2p.wallet.auth.ui.generalerror

import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.components.UiKitButton
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ButtonAction
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.ui.animationscreen.AnimationProgressFragment
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorContract.Presenter
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.auth.ui.restore_error.RestoreErrorScreenFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingGeneralErrorBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.root.SystemIconsStyle
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

        fun create(error: GatewayHandledState) =
            OnboardingGeneralErrorFragment()
                .withArgs(ARG_ERROR_STATE to error)
    }

    private val binding: FragmentOnboardingGeneralErrorBinding by viewBinding()

    private val screenError: GatewayHandledState by args(ARG_ERROR_STATE)

    override val presenter: Presenter by inject { parametersOf(screenError) }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

    private val signInHelper: GoogleSignInHelper by inject()
    override val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.WHITE
    override val customStatusBarStyle = SystemIconsStyle.BLACK
    override val customNavigationBarStyle = SystemIconsStyle.WHITE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.menu_onboarding_help)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.helpItem) {
                IntercomService.showMessenger()
            }
            return@setOnMenuItemClickListener true
        }
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            val systemAndIme = insets.systemAndIme()
            rootView.updatePadding(top = systemAndIme.top)
            binding.containerBottomButtons.updatePadding(bottom = systemAndIme.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun setState(state: GatewayHandledState.TitleSubtitleError): Unit = with(binding) {
        textViewErrorTitle.text = state.title
        textViewErrorSubtitle.text = state.subtitle
        state.email?.let {
            textViewErrorEmail.text = it
            textViewErrorEmail.isVisible = true
        }

        state.googleButton?.let { buttonState ->
            buttonRestoreByGoogle.setText(buttonState.titleResId)
            buttonState.iconResId?.let { buttonRestoreByGoogle.setIconResource(it) }
            buttonState.iconTintResId?.let {
                buttonRestoreByGoogle.setIconResource(it)
            }
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

    private fun setButtonAction(button: UiKitButton, action: ButtonAction) {
        button.setOnClickListener {
            when (action) {
                ButtonAction.OPEN_INTERCOM -> {
                    IntercomService.showMessenger()
                }
                ButtonAction.NAVIGATE_ENTER_PHONE -> {
                    presenter.onEnterPhoneClicked()
                }
                ButtonAction.NAVIGATE_START_SCREEN -> {
                    presenter.onStartScreenClicked()
                }
                ButtonAction.NAVIGATE_GOOGLE_AUTH -> {
                    presenter.onGoogleAuthClicked()
                }
            }
        }
    }

    override fun setState(state: GatewayHandledState.CriticalError) = with(binding) {
        textViewErrorTitle.text = resourcesProvider.getString(R.string.onboarding_general_error_critical_error_title)
        textViewErrorSubtitle.text = resourcesProvider.getString(
            R.string.onboarding_general_error_critical_error_sub_title,
            state.errorCode
        )
        with(buttonRestoreByGoogle) {
            setText(R.string.onboarding_general_error_bug_report_button_title)
            setIconResource(R.drawable.ic_caution)
            setOnClickListener { IntercomService.showMessenger() }
            isVisible = true
        }
        with(buttonSecondaryFirst) {
            setText(R.string.restore_starting_screen)
            setOnClickListener { navigateToStartScreen() }
            isVisible = true
        }
        imageViewBox.setImageResource(R.drawable.ic_timer_error)
    }

    override fun navigateToEnterPhone() {
        popAndReplaceFragment(
            PhoneNumberEnterFragment.create(),
            inclusive = true
        )
    }

    override fun navigateToStartScreen() {
        popAndReplaceFragment(
            OnboardingRootFragment.create(),
            inclusive = true
        )
    }

    override fun startGoogleFlow() {
        signInHelper.showSignInDialog(requireContext(), googleSignInLauncher)
    }

    override fun setLoadingState(isLoading: Boolean) {
        setLoadingAnimationState(isScreenLoading = isLoading)
        binding.buttonRestoreByGoogle.isLoadingState = isLoading
    }

    override fun navigateToPinCreate() {
        popAndReplaceFragment(NewCreatePinFragment.create(), inclusive = true)
    }

    override fun restartWithState(state: RestoreFailureState.TitleSubtitleError) {
        popAndReplaceFragment(RestoreErrorScreenFragment.create(state), inclusive = true)
    }

    private fun handleSignResult(result: ActivityResult) {
        signInHelper.parseSignInResult(requireContext(), result, errorHandler = this)?.let { credential ->
            presenter.setGoogleIdToken(credential.id, credential.googleIdToken.orEmpty())
        }
    }

    override fun onConnectionError() {
        setRestoreByGoogleLoadingState(isLoading = false)
        showUiKitSnackBar(messageResId = R.string.common_offline_error)
    }

    override fun onCommonError() {
        setRestoreByGoogleLoadingState(isLoading = false)
        showUiKitSnackBar(messageResId = R.string.onboarding_google_services_error)
    }

    private fun setRestoreByGoogleLoadingState(isLoading: Boolean) {
        setLoadingAnimationState(isScreenLoading = isLoading)
        binding.buttonRestoreByGoogle.isLoadingState = isLoading
    }

    private fun setLoadingAnimationState(isScreenLoading: Boolean) {
        if (isScreenLoading) {
            AnimationProgressFragment.show(requireActivity().supportFragmentManager, isCreation = false)
        } else {
            AnimationProgressFragment.dismiss(requireActivity().supportFragmentManager)
        }
    }
}
