package org.p2p.wallet.transaction.ui

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Locale
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogJupiterSwapTransactionProgressBinding
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.transaction.model.TransactionStateSwapFailureReason
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_DATA = "EXTRA_DATA"
private const val EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID"

private const val IMAGE_SIZE = 64
private const val DATE_FORMAT = "MMMM dd, yyyy"
private const val TIME_FORMAT = "HH:mm"

private const val SLIPPAGE_NEEDED_FOR_MANUAL_CHANGE: Double = 9.0

class JupiterTransactionProgressBottomSheet : BottomSheetDialogFragment() {
    companion object {
        fun show(
            fm: FragmentManager,
            transactionId: String,
            data: SwapTransactionBottomSheetData
        ) {
            JupiterTransactionProgressBottomSheet()
                .withArgs(
                    EXTRA_DATA to data,
                    EXTRA_TRANSACTION_ID to transactionId,
                )
                .show(fm, JupiterTransactionProgressBottomSheet::javaClass.name)
        }

        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(JupiterTransactionProgressBottomSheet::javaClass.name)
            (dialog as? JupiterTransactionProgressBottomSheet)?.dismissAllowingStateLoss()
        }
    }

    private val transactionManager: TransactionManager by inject()
    private val glideManager: GlideManager by inject()

    private val binding: DialogJupiterSwapTransactionProgressBinding by viewBinding()

    private val data: SwapTransactionBottomSheetData by args(EXTRA_DATA)
    private val transactionId: String by args(EXTRA_TRANSACTION_ID)

    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
    private val timeFormat = SimpleDateFormat(TIME_FORMAT, Locale.US)

    private val progressStateFormat: String by unsafeLazy { getString(R.string.transaction_progress_title) }

    private var parentListener: JupiterTransactionBottomSheetDismissListener? = null
    private var dismissResult: JupiterTransactionDismissResult = JupiterTransactionDismissResult.TransactionInProgress

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentListener = parentFragment as? JupiterTransactionBottomSheetDismissListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_jupiter_swap_transaction_progress, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            textViewSubtitle.text = getString(
                R.string.transaction_date_format,
                dateFormat.format(data.date),
                timeFormat.format(data.date)
            )

            glideManager.load(
                imageView = imageViewFirstIcon,
                url = data.tokenA.tokenUrl,
                size = IMAGE_SIZE,
                circleCrop = true
            )
            glideManager.load(
                imageView = imageViewSecondIcon,
                url = data.tokenB.tokenUrl,
                size = IMAGE_SIZE,
                circleCrop = true
            )
            textViewAmountUsd.text = "$ ${data.amountUsd}"
            textViewAmountTokens.text = getString(
                R.string.swap_transaction_details_token_amounts,
                data.tokenA.formattedTokenAmount,
                data.tokenA.tokenName,
                data.tokenB.formattedTokenAmount,
                data.tokenB.tokenName
            )

            textViewFeeValue.text = getString(R.string.transaction_transaction_fee_free_value)

            buttonDone.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }

        observeState()
    }

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(requireView().parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    private fun isBottomSheetDraggable(isDraggable: Boolean) {
        BottomSheetBehavior.from(requireView().parent as View).isDraggable = isDraggable
    }

    private fun observeState() {
        lifecycleScope.launchWhenCreated {
            transactionManager.getTransactionStateFlow(transactionId).collect { state ->
                TransitionManager.beginDelayedTransition(binding.root)
                isBottomSheetDraggable(
                    state is TransactionState.Progress ||
                        state is TransactionState.JupiterSwapSuccess
                )

                when (state) {
                    is TransactionState.Progress -> setProgressState()
                    is TransactionState.JupiterSwapSuccess -> setSuccessState()
                    is TransactionState.JupiterSwapFailed -> setErrorState(state.failure)
                    else -> error("Not supported transaction state for this details: $state")
                }
            }
        }
    }

    private fun setProgressState() {
        dismissResult = JupiterTransactionDismissResult.TransactionInProgress
        with(binding) {
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_submitted))
            progressStateTransaction.setDescriptionText(R.string.transaction_description_progress)
            buttonDone.setText(R.string.common_done)
        }
    }

    private fun setSuccessState() {
        dismissResult = JupiterTransactionDismissResult.TransactionSuccess
        with(binding) {
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_succeeded))
            progressStateTransaction.setSuccessState()
            progressStateTransaction.setDescriptionText(R.string.transaction_description_succeeded)
            buttonDone.setText(R.string.common_done)
        }
    }

    private fun setErrorState(reason: TransactionStateSwapFailureReason) {
        binding.textViewAmountUsd.setTextColorRes(R.color.text_rose)
        when (reason) {
            is TransactionStateSwapFailureReason.LowSlippage -> setLowSlippageError(reason)
            is TransactionStateSwapFailureReason.Unknown -> setUnknownTransactionError()
        }
    }

    private fun setLowSlippageError(error: TransactionStateSwapFailureReason.LowSlippage) = with(binding) {
        if (error.currentSlippageValue.doubleValue <= SLIPPAGE_NEEDED_FOR_MANUAL_CHANGE) {
            val newSlippage = getNewSlippage(error.currentSlippageValue.doubleValue)
            progressStateTransaction.setDescriptionText(
                getString(
                    R.string.swap_transaction_details_error_low_slippage,
                    error.currentSlippageValue.toString(),
                    newSlippage.toString()
                )
            )

            dismissResult = JupiterTransactionDismissResult.SlippageChangeNeeded(newSlippage)
        } else {
            progressStateTransaction.setDescriptionText(
                getString(
                    R.string.swap_transaction_details_error_low_slippage_manual,
                    error.currentSlippageValue.toString()
                )
            )
            dismissResult = JupiterTransactionDismissResult.ManualSlippageChangeNeeded
        }
        textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_failed))
        progressStateTransaction.setFailedState()

        buttonDone.setText(R.string.swap_transaction_details_error_low_slippage_button)
    }

    private fun setUnknownTransactionError() = with(binding) {
        dismissResult = JupiterTransactionDismissResult.TrySwapAgain

        textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_failed))
        progressStateTransaction.setFailedState()
        progressStateTransaction.setDescriptionText(R.string.swap_transaction_details_error_unknown)
        buttonDone.setText(R.string.common_try_again)
    }

    private fun getNewSlippage(currentSlippage: Double): Double = when (currentSlippage) {
        in (0.0..0.4) -> 0.5
        in (0.5..0.9) -> 1.0
        in (1.0..4.9) -> 5.0
        in (5.0..9.9) -> 10.0
        else -> currentSlippage
    }

    override fun dismissAllowingStateLoss() {
        parentListener?.onBottomSheetDismissed(dismissResult)
        super.dismissAllowingStateLoss()
    }

    override fun dismiss() {
        parentListener?.onBottomSheetDismissed(dismissResult)
        super.dismiss()
    }

    override fun onDestroy() {
        parentListener = null
        super.onDestroy()
    }
}
