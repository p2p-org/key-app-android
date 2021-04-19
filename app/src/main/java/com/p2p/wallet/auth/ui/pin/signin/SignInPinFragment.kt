package com.p2p.wallet.auth.ui.pin.signin

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentSignInPinBinding
import com.p2p.wallet.main.ui.MainFragment
import com.p2p.wallet.restore.ui.secretkeys.view.SecretKeyFragment
import com.p2p.wallet.utils.BiometricPromptWrapper
import com.p2p.wallet.utils.edgetoedge.Edge
import com.p2p.wallet.utils.edgetoedge.edgeToEdge
import com.p2p.wallet.utils.popAndReplaceFragment
import com.p2p.wallet.utils.vibrate
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject
import javax.crypto.Cipher

class SignInPinFragment :
    BaseMvpFragment<SignInPinContract.View, SignInPinContract.Presenter>(R.layout.fragment_sign_in_pin),
    SignInPinContract.View {

    companion object {
        fun create() = SignInPinFragment()
    }

    override val presenter: SignInPinContract.Presenter by inject()

    private val binding: FragmentSignInPinBinding by viewBinding()

    private val biometricWrapper by lazy {
        BiometricPromptWrapper(this) { presenter.signInByBiometric(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }

        with(binding) {
            edgeToEdge {
                contentView.fit { Edge.All }
            }
            pinView.onBiometricClicked = { presenter.onBiometricSignInRequested() }
            pinView.onPinCompleted = { presenter.signIn(it) }
            pinView.onResetClicked = { popAndReplaceFragment(SecretKeyFragment.create()) }
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.pinView.showLoading(isLoading)
    }

    override fun onStart() {
        super.onStart()
        presenter.checkIfBiometricAvailable()
    }

    override fun showBiometricDialog(cipher: Cipher) {
        biometricWrapper.authenticate(cipher)
    }

    override fun onSignInSuccess() {
        popAndReplaceFragment(MainFragment.create(), inclusive = true)
    }

    override fun onLogout() {
        popAndReplaceFragment(
            OnboardingFragment.create(),
            popTo = OnboardingFragment::class,
            addToBackStack = false,
            inclusive = true,
            enter = 0
        )
    }

    override fun showWrongPinError(attemptsLeft: Int) {
        val message = getString(R.string.auth_pin_code_attempts_format, attemptsLeft)
        binding.pinView.startErrorAnimation(message)
    }

    override fun showWalletLocked() {
        val message = getString(R.string.auth_locked_message)
        binding.pinView.showLockedState(message)
    }

    override fun vibrate(duration: Long) {
        requireContext().vibrate(duration)
    }

    override fun clearPin() {
        binding.pinView.clearPin()
    }
}