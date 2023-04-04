package org.p2p.wallet.auth.ui.restore_error

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.components.UiKitButton
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ButtonAction
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.ui.animationscreen.AnimationProgressFragment
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentRestoreErrorScreenBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_RESTORE_STATE = "ARG_RESTORE_STATE"

class RestoreErrorScreenFragment :
    BaseMvpFragment<RestoreErrorScreenContract.View, RestoreErrorScreenContract.Presenter>(
        R.layout.fragment_restore_error_screen
    ),
    GoogleSignInHelper.GoogleSignInErrorHandler,
    RestoreErrorScreenContract.View {

    companion object {
        fun create(restoreState: RestoreFailureState.TitleSubtitleError) =
            RestoreErrorScreenFragment().withArgs(
                ARG_RESTORE_STATE to restoreState
            )
    }

    override val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.WHITE
    override val customStatusBarStyle = SystemIconsStyle.BLACK
    override val customNavigationBarStyle = SystemIconsStyle.WHITE

    override val presenter: RestoreErrorScreenContract.Presenter by inject { parametersOf(restoreState) }
    private val binding: FragmentRestoreErrorScreenBinding by viewBinding()

    private val signInHelper: GoogleSignInHelper by inject()
    private val restoreState: RestoreFailureState.TitleSubtitleError by args(ARG_RESTORE_STATE)
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

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
            binding.containerBottomButton.updatePadding(bottom = systemAndIme.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onConnectionError() {
        setRestoreByGoogleLoadingState(isLoading = false)
        showUiKitSnackBar(message = getString(R.string.error_general_message))
    }

    override fun onCommonError() {
        setRestoreByGoogleLoadingState(isLoading = false)
        showUiKitSnackBar(messageResId = R.string.onboarding_google_services_error)
    }

    private fun handleSignResult(result: ActivityResult) {
        signInHelper.parseSignInResult(requireContext(), result, errorHandler = this)?.let { credential ->
            presenter.setGoogleIdToken(credential.id, credential.googleIdToken.orEmpty())
        }
    }

    private fun setRestoreByGoogleLoadingState(isLoading: Boolean) {
        binding.buttonRestoreByGoogle.setLoading(isLoading)
    }

    override fun showState(state: RestoreFailureState.TitleSubtitleError): Unit = with(binding) {
        textViewErrorTitle.text = state.title
        textViewErrorSubtitle.text = state.subtitle
        state.email?.let {
            textViewErrorEmail.text = it
            textViewErrorEmail.isVisible = true
        }
        if (state.imageViewResId != null) {
            imageViewBox.setImageResource(state.imageViewResId)
        } else {
            imageViewBox.setImageResource(R.drawable.ic_cat)
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

    override fun startGoogleFlow() {
        signInHelper.showSignInDialog(requireContext(), googleSignInLauncher)
    }

    override fun setLoadingState(isLoading: Boolean) {
        setLoadingAnimationState(isScreenLoading = isLoading)
        binding.buttonRestoreByGoogle.setLoading(isLoading)
    }

    override fun navigateToPinCreate() {
        popAndReplaceFragment(NewCreatePinFragment.create(), inclusive = true)
    }

    override fun navigateToPhoneEnter() {
        popAndReplaceFragment(PhoneNumberEnterFragment.create(), inclusive = true)
    }

    override fun navigateToStartScreen() {
        popAndReplaceFragment(
            OnboardingRootFragment.create(),
            inclusive = true
        )
    }

    override fun restartWithState(state: RestoreFailureState.TitleSubtitleError) {
        popAndReplaceFragment(create(state))
    }

    private fun setButtonAction(button: UiKitButton, action: ButtonAction) {
        button.setOnClickListener {
            when (action) {
                ButtonAction.OPEN_INTERCOM -> {
                    IntercomService.showMessenger()
                }
                ButtonAction.NAVIGATE_GOOGLE_AUTH -> {
                    presenter.useGoogleAccount()
                }
                ButtonAction.NAVIGATE_ENTER_PHONE -> {
                    presenter.useCustomShare()
                }
                ButtonAction.NAVIGATE_START_SCREEN -> {
                    presenter.onStartScreenClicked()
                }
            }
        }
    }

    private fun setLoadingAnimationState(isScreenLoading: Boolean) {
        if (isScreenLoading) {
            AnimationProgressFragment.show(requireActivity().supportFragmentManager, isCreation = false)
        } else {
            AnimationProgressFragment.dismiss(requireActivity().supportFragmentManager)
        }
    }
}
