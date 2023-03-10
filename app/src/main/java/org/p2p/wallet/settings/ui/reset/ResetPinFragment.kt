package org.p2p.wallet.settings.ui.reset

import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import org.koin.android.ext.android.inject
import javax.crypto.Cipher
import org.p2p.uikit.utils.SpanUtils
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentChangePinBinding
import org.p2p.wallet.settings.ui.reset.seedphrase.ResetSeedPhraseFragment
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.vibrate
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_REQUEST_KEY = "EXTRA_RESET_PIN_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

@Deprecated("Old onboarding flow, delete someday")
class ResetPinFragment :
    BaseMvpFragment<ResetPinContract.View, ResetPinContract.Presenter>(R.layout.fragment_change_pin),
    ResetPinContract.View {

    companion object {
        fun create(requestKey: String, resultKey: String) = ResetPinFragment().withArgs(
            EXTRA_REQUEST_KEY to requestKey,
            EXTRA_RESULT_KEY to resultKey
        )
    }

    override val presenter: ResetPinContract.Presenter by inject()
    private val binding: FragmentChangePinBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val authAnalytics: AuthAnalytics by inject()

    private val biometricWrapper by lazy {
        BiometricPromptWrapper(
            this,
            onError = { presenter.resetPinWithoutBiometrics() },
            onSuccess = { presenter.resetPinWithBiometrics(it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.PIN_ENTER)
        with(binding) {
            requireContext()
            toolbar.setNavigationOnClickListener { popBackStack() }
            pinView.onPinCompleted = { presenter.setPinCode(it) }
            resetTextView.text = buildResetText()
            resetTextView.setOnClickListener {
                onResetClicked()
            }
        }
        requireActivity().supportFragmentManager.setFragmentResultListener(
            EXTRA_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            when {
                result.containsKey(EXTRA_RESULT_KEY) -> {
                    val keys = result.getStringArrayList(EXTRA_RESULT_KEY)
                    if (keys != null) presenter.onSeedPhraseValidated(keys)
                }
            }
        }
    }

    override fun showResetSuccess() {
        setFragmentResult(
            requestKey,
            bundleOf(Pair(resultKey, true))
        )
        popBackStack()
    }

    override fun showEnterNewPin() {
        binding.resetTextView.isVisible = false
        binding.toolbar.setTitle(R.string.settings_security_change_enter_pin)
        binding.pinView.clearPin()
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.PIN_CREATE)
    }

    override fun showConfirmationError() {
        val message = getString(R.string.settings_security_pin_codes_matching)
        binding.pinView.startErrorAnimation(message)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.pinView.showLoading(isLoading)
    }

    override fun showCurrentPinIncorrectError() {
        val message = getString(R.string.auth_pin_code_wrong)
        binding.pinView.startErrorAnimation(message)
    }

    override fun showConfirmNewPin() {
        binding.toolbar.setTitle(R.string.settings_security_change_confirm_pin)
        binding.pinView.clearPin()
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.PIN_CONFIRM)
    }

    override fun showWalletLocked(seconds: Long) {
        val message = getString(R.string.auth_locked_message, seconds.toString())
        binding.pinView.showLockedState(message)
    }

    override fun showWalletUnlocked() {
        binding.pinView.showUnlockedState()
    }

    override fun clearPin() {
        binding.pinView.clearPin()
    }

    override fun showBiometricDialog(cipher: Cipher) {
        biometricWrapper.authenticate(cipher)
    }

    override fun vibrate(duration: Long) {
        requireContext().vibrate(duration)
    }

    private fun buildResetText(): SpannableString {
        val message = getString(R.string.settings_forgot_your_pin)
        val resetMessage = getString(R.string.settings_reset_it)

        val span = SpanUtils.setTextBold(message, resetMessage)

        val clickableReset = object : ClickableSpan() {
            override fun onClick(widget: View) {
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        val resetStart = span.indexOf(resetMessage)
        val resetEnd = span.indexOf(resetMessage) + resetMessage.length
        span.setSpan(clickableReset, resetStart, resetEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return span
    }

    private fun onResetClicked() {
        replaceFragment(
            ResetSeedPhraseFragment.create(
                EXTRA_REQUEST_KEY,
                EXTRA_RESULT_KEY
            )
        )
        authAnalytics.logAuthResetInvoked()
    }
}
