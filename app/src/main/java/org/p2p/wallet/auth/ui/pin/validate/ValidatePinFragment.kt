package org.p2p.wallet.auth.ui.pin.validate

import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.root.OnboardingRootFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSignInPinBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.vibrate
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import javax.crypto.Cipher

private const val EXTRA_REQUEST_KEY = "EXTRA_IS_SIGN_SUCCESS_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class ValidatePinFragment :
    BaseMvpFragment<ValidatePinContract.View, ValidatePinContract.Presenter>(R.layout.fragment_sign_in_pin),
    ValidatePinContract.View {

    companion object {
        fun create(
            requestKey: String,
            resultKey: String
        ): ValidatePinFragment = ValidatePinFragment().withArgs(
            EXTRA_REQUEST_KEY to requestKey,
            EXTRA_RESULT_KEY to resultKey
        )
    }

    override val presenter: ValidatePinContract.Presenter by inject()
    private val binding: FragmentSignInPinBinding by viewBinding()

    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)

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
            onDismiss()
        }
        with(binding) {
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.helpItem) {
                    IntercomService.showMessenger()
                    true
                } else {
                    false
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
        setFragmentResult(requestKey, bundleOf(resultKey to true))
        popBackStack()
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

    private fun onDismiss() {
        setFragmentResult(resultKey, bundleOf(resultKey to false))
        popBackStack()
    }
}
