package org.p2p.wallet.auth.ui.pin.signin

import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSignInPinBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.vibrate
import org.p2p.wallet.utils.viewbinding.viewBinding
import javax.crypto.Cipher

class SignInPinFragment :
    BaseMvpFragment<SignInPinContract.View, SignInPinContract.Presenter>(R.layout.fragment_sign_in_pin),
    SignInPinContract.View {

    companion object {
        fun create(): SignInPinFragment = SignInPinFragment()
    }

    override val presenter: SignInPinContract.Presenter by inject()
    private val binding: FragmentSignInPinBinding by viewBinding()
    private val biometricWrapper by lazy {
        BiometricPromptWrapper(
            this,
            onLockout = {
                showDialog(
                    title = R.string.auth_pin_code_lockout_title,
                    message = R.string.auth_pin_code_lockout_description,
                    positiveButtonString = R.string.auth_pin_code_lockout_ok_button
                )
            },
            onSuccess = { presenter.signInByBiometric(it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
        with(binding) {
            with(toolbar) {
                setOnMenuItemClickListener {
                    if (it.itemId == R.id.helpItem) {
                        IntercomService.showMessenger()
                        true
                    } else {
                        false
                    }
                }
            }
            pinView.onBiometricClicked = { presenter.onBiometricSignInRequested() }
            pinView.onPinCompleted = { presenter.signIn(it) }
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.checkIfBiometricAvailable()
    }

    override fun showBiometricDialog(cipher: Cipher) {
        biometricWrapper.authenticate(cipher)
    }

    override fun onSignInSuccess() {
        binding.pinView.onSuccessPin()
        popAndReplaceFragment(MainFragment.create(), inclusive = true)
    }

    override fun onLogout() {
        popAndReplaceFragment(
            target = OnboardingRootFragment.create(),
            addToBackStack = false,
            inclusive = true,
            enter = 0
        )
        showDialog(
            title = R.string.auth_pin_code_logout_title,
            message = R.string.auth_pin_code_logout_description,
            positiveButtonString = R.string.auth_pin_code_logout_ok_button
        )
    }

    private fun showDialog(
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes positiveButtonString: Int
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonString) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun setBiometricVisibility(isVisible: Boolean) {
        binding.pinView.setFingerprintVisible(isVisible)
    }

    override fun showWrongPinError(attemptsLeft: Int) {
        val message = getString(R.string.auth_pin_code_attempts_format, attemptsLeft)
        showUiKitSnackBar(message)
        binding.pinView.startErrorAnimation()
    }

    override fun showWarnPinError(attemptsLeft: Int) {
        val message = getString(R.string.auth_pin_code_warn_format, attemptsLeft)
        showUiKitSnackBar(message)
        binding.pinView.startErrorAnimation()
    }

    override fun showWalletLocked(seconds: Long) {
        val message = getString(R.string.auth_locked_message, seconds.toString())
        showUiKitSnackBar(message)
        binding.pinView.showLockedState()
    }

    override fun showWalletUnlocked() {
        binding.pinView.showUnlockedState()
    }

    override fun vibrate(duration: Long) {
        requireContext().vibrate(duration)
    }

    override fun clearPin() {
        binding.pinView.clearPin()
    }
}
