package org.p2p.wallet.send.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.SignInResult
import org.p2p.wallet.common.glide.GlideManager
import org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import org.p2p.wallet.databinding.DialogSendConfirmBinding
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.send.model.SendConfirmData
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.vibrate
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import timber.log.Timber

private const val EXTRA_DATA = "EXTRA_DATA"
private const val VIBRATE_DURATION = 500L

class SendConfirmBottomSheet(
    private val onConfirmed: () -> Unit
) : NonDraggableBottomSheetDialogFragment() {

    companion object {
        fun show(fragment: Fragment, data: SendConfirmData, onConfirmed: () -> Unit) {
            SendConfirmBottomSheet(onConfirmed)
                .withArgs(EXTRA_DATA to data)
                .show(fragment.childFragmentManager, SendConfirmBottomSheet::javaClass.name)
        }
    }

    private val binding: DialogSendConfirmBinding by viewBinding()

    private val data: SendConfirmData by args(EXTRA_DATA)

    private val glideManager: GlideManager by inject()

    private val authInteractor: AuthInteractor by inject()

    private val sendAnalytics: SendAnalytics by inject()

    private val dispatchers: CoroutineDispatchers by inject()

    private val biometricWrapper by lazy {
        BiometricPromptWrapper(
            fragment = this,
            negativeRes = R.string.auth_biometric_use_pin_code,
            onError = { toast(R.string.fingerprint_not_recognized) },
            usePinCode = { binding.pinView.isVisible = true },
            onSuccess = { confirm() }
        )
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_send_confirm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendAnalytics.logSendVerificationInvoked(AuthAnalytics.AuthType.BIOMETRIC)
        initPinView()
        with(binding) {
            glideManager.load(sourceImageView, data.token.iconUrl)
            amountTextView.text = data.getFormattedAmount()
            amountUsdTextView.text = data.getFormattedAmountUsd()
            destinationTextView.text = data.destination.highlightPublicKey(requireContext())

            val highlightText = getString(R.string.send_confirm_warning_highlight)
            val commonText = getString(R.string.send_confirm_warning)
            warningTextView.text = SpanUtils.setTextBold(commonText, highlightText)

            confirmButton.setOnClickListener {
                confirmBiometrics()
            }
        }
    }

    private fun initPinView() = with(binding) {
        pinView.apply {
            setFingerprintVisible(true)
            onPinCompleted = ::checkPinCode
            onBiometricClicked = {
                isVisible = false
                confirmBiometrics()
            }
        }
    }

    private fun confirm() {
        onConfirmed.invoke()
        dismissAllowingStateLoss()
    }

    private fun checkPinCode(pinCode: String) {
        CoroutineScope(dispatchers.io).launch {
            when (authInteractor.signInByPinCode(pinCode)) {
                SignInResult.Success -> confirm()
                SignInResult.WrongPin -> withContext(dispatchers.ui) { showWrongPinError() }
            }
        }
    }

    private fun showWrongPinError() {
        val message = getString(R.string.auth_pin_code_wrong_pin)
        context?.vibrate(VIBRATE_DURATION)
        binding.pinView.startErrorAnimation(message)
    }

    private fun confirmBiometrics() {
        try {
            val cipher = authInteractor.getPinEncodeCipher()
            biometricWrapper.authenticate(cipher.value)
        } catch (e: Throwable) {
            Timber.wtf(e, "Unexpected error confirming biometrics")
            toast(R.string.default_error_msg)
        }
    }
}
