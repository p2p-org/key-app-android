package org.p2p.wallet.auth.ui.pin.signin

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSignInPinBinding
import org.p2p.wallet.restore.ui.keys.SecretKeyFragment
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.vibrate
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.home.MainFragment
import javax.crypto.Cipher

class SignInPinFragment :
    BaseMvpFragment<SignInPinContract.View, SignInPinContract.Presenter>(R.layout.fragment_sign_in_pin),
    SignInPinContract.View {

    companion object {
        fun create() = SignInPinFragment()
    }

    override val presenter: SignInPinContract.Presenter by inject()
    private val binding: FragmentSignInPinBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val biometricWrapper by lazy {
        BiometricPromptWrapper(this) { presenter.signInByBiometric(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Lock.SCREEN)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
        with(binding) {
            pinView.onBiometricClicked = { presenter.onBiometricSignInRequested() }
            pinView.onPinCompleted = { presenter.signIn(it) }
            pinView.onResetClicked = { popAndReplaceFragment(SecretKeyFragment.create()) }
        }
        presenter.load()
    }

    override fun showLoading(isLoading: Boolean) {
        binding.pinView.showLoading(isLoading)
    }

    override fun onStart() {
        super.onStart()
        presenter.checkIfBiometricAvailable()
    }

    override fun onStop() {
        super.onStop()
        presenter.stopTimer()
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

    override fun showWalletLocked(seconds: Long) {
        val message = getString(R.string.auth_locked_message, seconds.toString())
        binding.pinView.showLockedState(message)
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
