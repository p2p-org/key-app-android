package org.p2p.wallet.auth.ui.generalerror

import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorContract.Presenter
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentOnboardingGeneralErrorBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getDrawableCompat
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.auth.ui.generalerror.OnboardingGeneralErrorContract.View as ContractView

private const val ARG_ERROR_TYPE = "ARG_ERROR_TYPE"

class OnboardingGeneralErrorFragment :
    BaseMvpFragment<ContractView, Presenter>(R.layout.fragment_onboarding_general_error),
    ContractView,
    GoogleSignInHelper.GoogleSignInErrorHandler {

    companion object {
        fun create(error: GeneralErrorScreenError): OnboardingGeneralErrorFragment =
            OnboardingGeneralErrorFragment()
                .withArgs(ARG_ERROR_TYPE to error)
    }

    private val binding: FragmentOnboardingGeneralErrorBinding by viewBinding()

    private val screenError: GeneralErrorScreenError by args(ARG_ERROR_TYPE)

    override val presenter: Presenter by inject { parametersOf(screenError) }

    private val signInHelper: GoogleSignInHelper by inject()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

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
                imageViewBox.setImageResource(R.drawable.ic_not_found)
                with(buttonPrimaryFirst) {
                    text = getString(R.string.onboarding_general_error_bug_report_button_title)
                    setOnClickListener { IntercomService.showMessenger() }
                    isVisible = true
                }
                with(buttonSecondaryFirst) {
                    text = getString(R.string.onboarding_general_error_starting_screen_button_title)
                    setOnClickListener { popAndReplaceFragment(OnboardingRootFragment.create(), inclusive = true) }
                    isVisible = true
                }
            }
            is GeneralErrorScreenError.PhoneNumberDoesNotMatchError -> {
                errorState.titleResId?.let { textViewErrorTitle.text = getString(it) }
                errorState.messageResId?.let { textViewErrorSubtitle.text = getString(it) }

                imageViewBox.setImageResource(R.drawable.ic_cat)

                with(buttonRestoreByGoogle) {
                    setOnClickListener { presenter.useGoogleAccount() }
                    isVisible = true
                }
                with(buttonPrimaryFirst) {
                    text = getString(R.string.restore_phone_number)
                    setOnClickListener {
                        popAndReplaceFragment(PhoneNumberEnterFragment.create(), inclusive = true)
                    }
                    isVisible = true
                }
                with(buttonSecondaryFirst) {
                    text = getString(R.string.onboarding_general_error_starting_screen_button_title)
                    setOnClickListener { popAndReplaceFragment(OnboardingRootFragment.create(), inclusive = true) }
                    isVisible = true
                }
            }
            is GeneralErrorScreenError.AccountNotFound -> {
                onAccountNotFound(errorState)
            }
            is GeneralErrorScreenError.DeviceShareNotFound -> {
                onDeviceShareNotFound()
            }
            is GeneralErrorScreenError.NoTokenFound -> {
                onNoTokenFound(errorState.tokenId)
            }
        }
        val imageResourceId = when (errorState) {
            GeneralErrorScreenError.DeviceShareNotFound -> R.drawable.easy_to_start
            else -> R.drawable.onboarding_box
        }
        binding.imageViewBox.setImageResource(imageResourceId)
    }

    private fun onNoTokenFound(userId: String) {
        view?.post {
            with(binding) {
                textViewErrorEmail.apply {
                    isVisible = true
                    text = userId
                }
                textViewErrorSubtitle.text = getString(R.string.restore_no_wallet_try_another_option)
            }
            setRestoreByGoogleLoadingState(isRestoringByGoogle = false)
        }
    }

    private fun onAccountNotFound(errorState: GeneralErrorScreenError.AccountNotFound) = with(binding) {
        val isDeviceShareSaved = errorState.isDeviceShareExists
        val title = if (isDeviceShareSaved) {
            resourcesProvider.getString(R.string.restore_no_wallet_title)
        } else {
            resourcesProvider.getString(R.string.restore_no_account_title)
        }
        val message = if (isDeviceShareSaved) {
            resourcesProvider.getString(R.string.restore_no_wallet_found_with_device_share_message)
        } else {
            resourcesProvider.getString(
                R.string.restore_no_wallet_found_with_no_device_share_message,
                errorState.userPhoneNumber.formattedValue
            )
        }
        textViewErrorTitle.text = title
        textViewErrorSubtitle.text = message
        imageViewBox.setImageResource(R.drawable.onboarding_box)
        with(buttonRestoreByGoogle) {
            text = if (errorState.isDeviceShareExists) {
                getString(R.string.restore_continue_with_google)
            } else {
                getString(R.string.restore_another_phone_number)
            }

            icon = if (errorState.isDeviceShareExists) {
                context.getDrawableCompat(R.drawable.ic_google_logo)
            } else {
                null
            }

            setOnClickListener {
                if (errorState.isDeviceShareExists) {
                    presenter.useGoogleAccount()
                } else {
                    popAndReplaceFragment(PhoneNumberEnterFragment.create(), inclusive = true)
                }
            }
            isVisible = true
        }
        if (errorState.isDeviceShareExists) {
            with(buttonPrimaryFirst) {
                text = getString(R.string.restore_phone_number)
                setOnClickListener {
                    popAndReplaceFragment(
                        PhoneNumberEnterFragment.create(),
                        inclusive = true
                    )
                }
                isVisible = true
            }
        }

        with(buttonSecondaryFirst) {
            text = getString(R.string.onboarding_continue_starting_button_text)
            setOnClickListener {
                popAndReplaceFragment(
                    OnboardingRootFragment.create(),
                    inclusive = true
                )
            }
            isVisible = true
        }
    }

    private fun onDeviceShareNotFound() = with(binding) {
        with(buttonRestoreByGoogle) {
            setOnClickListener { presenter.useGoogleAccount() }
            isVisible = true
        }
        with(toolbar) {
            inflateMenu(R.menu.menu_onboarding_help)
            setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.helpItem) {
                    IntercomService.showMessenger()
                    return@setOnMenuItemClickListener true
                }
                return@setOnMenuItemClickListener false
            }
            isVisible = true
        }
        textViewErrorTitle.setText(R.string.restore_how_to_continue)
    }

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
            buttonPrimaryFirst.isVisible = true
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

    override fun onNoTokenFoundError(userId: String) {
        popAndReplaceFragment(create(GeneralErrorScreenError.NoTokenFound(userId)))
    }

    override fun navigateToPinCreate() {
        popAndReplaceFragment(NewCreatePinFragment.create(), inclusive = true)
    }
}
