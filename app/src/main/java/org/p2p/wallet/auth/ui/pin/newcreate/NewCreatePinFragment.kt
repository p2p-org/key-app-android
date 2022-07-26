package org.p2p.wallet.auth.ui.pin.newcreate

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import org.koin.android.ext.android.inject
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.done.AuthDoneFragment
import org.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewCreatePinBinding
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.vibrate
import org.p2p.wallet.utils.viewbinding.viewBinding
import javax.crypto.Cipher

class NewCreatePinFragment :
    BaseMvpFragment<NewCreatePinContract.View, NewCreatePinContract.Presenter>(R.layout.fragment_new_create_pin),
    NewCreatePinContract.View {

    companion object {
        fun create() = NewCreatePinFragment()
    }

    override val presenter: NewCreatePinContract.Presenter by inject()

    private val binding: FragmentNewCreatePinBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val biometricWrapper by lazy {
        BiometricPromptWrapper(
            this,
            onError = { presenter.createPin(null) },
            onSuccess = { presenter.createPin(it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { presenter.clearUserData() }
            pinView.onPinCompleted = {
                presenter.setPinCode(it)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStackTo(OnboardingFragment::class)
        }
    }

    override fun navigateBack() {
        popBackStack()
    }

    override fun showLoading(isLoading: Boolean) {
        binding.pinView.showLoading(isLoading)
    }

    override fun showCreation() {
        with(binding) {
            pinView.isEnabled = true
            textViewTitle.text = getString(R.string.auth_create_wallet_set_up_your_pin)
            pinView.clearPin()
        }
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.PIN_CREATE)
    }

    override fun showConfirmation() {
        with(binding) {
            pinView.isEnabled = true
            textViewTitle.text = getString(R.string.auth_create_wallet_confirm_your_pin)
            pinView.clearPin()
        }
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.PIN_CONFIRM)
    }

    override fun onAuthFinished() {
        popAndReplaceFragment(AuthDoneFragment.create(), inclusive = true)
    }

    override fun onPinCreated() {
        binding.pinView.startSuccessAnimation() {}
        binding.root.showSnackbarShort(R.string.auth_create_pin_code_success)
        showInfoDialog(
            titleRes = R.string.auth_fingerprint_login,
            messageRes = R.string.auth_fingerprint_login_message,
            primaryButtonRes = R.string.common_continue,
            secondaryButtonRes = R.string.common_cancel,
            primaryCallback = { presenter.enableBiometric() },
            secondaryCallback = { presenter.createPin(null) },
            isCancelable = false
        )
    }

    override fun showConfirmationError() {
        binding.pinView.startErrorAnimation()
        binding.root.showSnackbarShort(R.string.auth_pin_codes_match_error)
    }

    override fun lockPinKeyboard() {
        binding.pinView.isEnabled = false
    }

    override fun vibrate(duration: Long) {
        requireContext().vibrate(duration)
    }

    override fun showBiometricDialog(cipher: Cipher) {
        biometricWrapper.authenticate(cipher)
    }
}
