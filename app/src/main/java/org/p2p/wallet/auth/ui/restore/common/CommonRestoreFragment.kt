package org.p2p.wallet.auth.ui.restore.common

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorFragment
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.auth.ui.restore_error.RestoreErrorScreenFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentCommonRestoreBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.restore.ui.seedphrase.SeedPhraseFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_SHOW_BACK_BUTTON = "ARG_DO_NOT_SHOW_BACK"

class CommonRestoreFragment :
    BaseMvpFragment<CommonRestoreContract.View, CommonRestoreContract.Presenter>(
        R.layout.fragment_common_restore
    ),
    CommonRestoreContract.View,
    GoogleSignInHelper.GoogleSignInErrorHandler {

    companion object {
        fun create(showBackButton: Boolean = true): CommonRestoreFragment = CommonRestoreFragment().withArgs(
            ARG_SHOW_BACK_BUTTON to showBackButton
        )

        fun createWithoutBack(): CommonRestoreFragment = create(showBackButton = false)
    }

    override val presenter: CommonRestoreContract.Presenter by inject()

    private val binding: FragmentCommonRestoreBinding by viewBinding()

    private val showBackButton: Boolean by args(ARG_SHOW_BACK_BUTTON)

    override val statusBarColor: Int = R.color.bg_lime
    override val navBarColor: Int = R.color.bg_night
    override val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.WHITE

    private val signInHelper: GoogleSignInHelper by inject()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            if (showBackButton) {
                toolbar.setNavigationIcon(R.drawable.ic_back)
            } else {
                toolbar.navigationIcon = null
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.helpItem) {
                    // pass empty string as UserId to launch IntercomService as anonymous user
                    IntercomService.signIn(userId = emptyString())
                    IntercomService.showMessenger()
                    return@setOnMenuItemClickListener true
                }
                false
            }
            buttonRestoreByGoogle.setOnClickListener {
                presenter.useGoogleAccount()
            }

            buttonPhone.setOnClickListener {
                presenter.useCustomShare()
            }

            buttonBottom.setOnClickListener {
                replaceFragment(SeedPhraseFragment.create())
            }
            buttonBottom.setOnLongClickListener {
                // TODO PWN-4615 remove after all onboarding testing completed!
                replaceFragment(DebugSettingsFragment.create())
                true
            }
        }

        presenter.switchFlowToRestore()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }

    override fun startGoogleFlow() {
        signInHelper.showSignInDialog(requireContext(), googleSignInLauncher)
    }

    override fun navigateToPinCreate() {
        popAndReplaceFragment(NewCreatePinFragment.create(), inclusive = true)
    }

    override fun showError(error: String) {
        view?.post {
            setLoadingState(isScreenLoading = false)
            showUiKitSnackBar(error)
        }
    }

    override fun onNoTokenFoundError(userId: String) {
        view?.post {
            with(binding) {
                imageView.setImageResource(R.drawable.image_box)
                textViewTitle.text = getString(R.string.restore_no_wallet_title)
                textViewSubtitle.apply {
                    isVisible = true
                    text = userId
                }
                textViewTryAnother.isVisible = true

                buttonBottom.apply {
                    strokeWidth = 0
                    text = getString(R.string.restore_starting_screen)
                    backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.transparent)
                    setTextColor(getColor(R.color.text_lime))
                    setOnClickListener {
                        popAndReplaceFragment(OnboardingRootFragment.create(), inclusive = true)
                    }
                }
            }
            setLoadingState(isScreenLoading = false)
        }
    }

    override fun setLoadingState(isScreenLoading: Boolean) {
        with(binding) {
            buttonRestoreByGoogle.apply {
                isLoadingState = isScreenLoading
                isEnabled = !isScreenLoading
            }
            buttonPhone.isEnabled = !isScreenLoading
            buttonBottom.isEnabled = !isScreenLoading
        }
    }

    override fun setRestoreViaGoogleFlowVisibility(isVisible: Boolean) {
        with(binding) {
            buttonRestoreByGoogle.isVisible = isVisible
            if (!isVisible) {

                buttonPhone.apply {
                    backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.bg_snow)
                    setTextColor(getColor(R.color.text_night))
                }
            }
        }
    }

    override fun showGeneralErrorScreen(handledState: GatewayHandledState) {
        popAndReplaceFragment(OnboardingGeneralErrorFragment.create(handledState), inclusive = true)
    }

    override fun showRestoreErrorScreen(handledState: RestoreFailureState.TitleSubtitleError) {
        popAndReplaceFragment(RestoreErrorScreenFragment.create(handledState), inclusive = true)
    }

    private fun handleSignResult(result: ActivityResult) {
        signInHelper.parseSignInResult(requireContext(), result, errorHandler = this)?.let { credential ->
            setLoadingState(isScreenLoading = true)
            presenter.setGoogleIdToken(credential.id, credential.googleIdToken.orEmpty())
        }
    }

    override fun onConnectionError() {
        setLoadingState(isScreenLoading = false)
        showUiKitSnackBar(message = getString(R.string.error_general_message))
    }

    override fun onCommonError() {
        setLoadingState(isScreenLoading = false)
        showUiKitSnackBar(messageResId = R.string.onboarding_google_services_error)
    }

    override fun navigateToPhoneEnter() {
        replaceFragment(PhoneNumberEnterFragment.create())
    }
}
