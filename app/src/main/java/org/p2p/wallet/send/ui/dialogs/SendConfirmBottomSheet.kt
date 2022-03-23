package org.p2p.wallet.send.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.glide.GlideManager
import org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import org.p2p.wallet.databinding.DialogSendConfirmBinding
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.send.model.SendConfirmData
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import timber.log.Timber

private const val EXTRA_DATA = "EXTRA_DATA"

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

    private val biometricWrapper by lazy {
        BiometricPromptWrapper(
            fragment = this,
            onError = { toast(R.string.fingerprint_not_recognized) },
            onSuccess = {
                onConfirmed.invoke()
                dismissAllowingStateLoss()
            }
        )
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_send_confirm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendAnalytics.logSendVerificationInvoked(AuthAnalytics.AuthType.BIOMETRIC)
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
