package org.p2p.wallet.swap.ui.orca

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koin.android.ext.android.inject
import timber.log.Timber
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import org.p2p.wallet.databinding.DialogSwapConfirmBinding
import org.p2p.wallet.swap.model.SwapConfirmData
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_DATA = "EXTRA_DATA"

class SwapConfirmBottomSheet(
    private val onConfirmed: () -> Unit
) : NonDraggableBottomSheetDialogFragment() {

    companion object {
        fun show(fragment: Fragment, data: SwapConfirmData, onConfirmed: () -> Unit) {
            SwapConfirmBottomSheet(onConfirmed)
                .withArgs(EXTRA_DATA to data)
                .show(fragment.childFragmentManager, SwapConfirmBottomSheet::javaClass.name)
        }
    }

    private val binding: DialogSwapConfirmBinding by viewBinding()
    private val data: SwapConfirmData by args(EXTRA_DATA)
    private val glideManager: GlideManager by inject()
    private val authInteractor: AuthInteractor by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

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

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_swap_confirm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Swap.CONFIRMATION)
        with(binding) {
            glideManager.load(sourceImageView, data.sourceToken.iconUrl)
            amountTextView.text = data.getFormattedSourceAmount()
            amountUsdTextView.text = data.getFormattedSourceAmountUsd()

            glideManager.load(destinationImageView, data.destinationToken.iconUrl)
            destinationAmountTextView.text = data.getFormattedDestinationAmount()
            destAmountUsdTextView.text = data.getFormattedDestinationAmountUsd()

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
