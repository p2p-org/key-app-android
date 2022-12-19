package org.p2p.wallet.transaction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogNewTransactionProgressBinding
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Bottom sheet dialog which shows current transaction's state
 * The default state when it's launched is [TransactionState.Progress]
 * */

private const val EXTRA_DATA = "EXTRA_DATA"
private const val EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID"

private const val IMAGE_SIZE = 64
private const val TIME_FORMAT = "HH:mm"

class NewTransactionProgressBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun show(
            fragmentManager: FragmentManager,
            transactionId: String,
            data: NewShowProgress
        ) {
            NewTransactionProgressBottomSheet()
                .withArgs(
                    EXTRA_DATA to data,
                    EXTRA_TRANSACTION_ID to transactionId
                )
                .show(fragmentManager, NewTransactionProgressBottomSheet::javaClass.name)
        }

        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(NewTransactionProgressBottomSheet::javaClass.name)
            (dialog as? NewTransactionProgressBottomSheet)?.dismissAllowingStateLoss()
        }
    }

    private val transactionManager: TransactionManager by inject()
    private val glideManager: GlideManager by inject()

    private val binding: DialogNewTransactionProgressBinding by viewBinding()

    private val data: NewShowProgress by args(EXTRA_DATA)
    private val transactionId: String by args(EXTRA_TRANSACTION_ID)

    private val dateFormat by unsafeLazy { SimpleDateFormat(TIME_FORMAT, Locale.getDefault()) }

    private lateinit var progressStateFormat: String

    private val colorMountain by unsafeLazy { getColor(R.color.text_mountain) }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_new_transaction_progress, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressStateFormat = getString(R.string.transaction_progress_title)
        with(binding) {
            textViewSubtitle.text = getString(R.string.transaction_date_format, dateFormat.format(data.date))
            glideManager.load(imageViewToken, data.tokenUrl, IMAGE_SIZE)
            textViewAmountUsd.text = data.amountUsd
            textViewAmountTokens.text = data.amountTokens
            textViewSendToValue.text = data.recipient
            val total = data.totalFee
            textViewFeeValue.text = if (total.sendFee != null) {
                total.getFeesCombined(colorMountain, checkFeePayer = false)
            } else {
                resources.getString(R.string.transaction_transaction_fee_free_value)
            }
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

    private fun observeState() {
        lifecycleScope.launchWhenCreated {
            transactionManager.getTransactionStateFlow(transactionId).collect { state ->
                TransitionManager.beginDelayedTransition(binding.root)
                when (state) {
                    is TransactionState.Progress -> handleProgress(state)
                    is TransactionState.SendSuccess -> handleSendSuccess(state)
                    is TransactionState.SwapSuccess -> handleSwapSuccess(state)
                    is TransactionState.Error -> handleError(state)
                }
            }
        }
    }

    private fun handleSwapSuccess(state: TransactionState.SwapSuccess) {
        val message = getString(R.string.swap_successfully_format, state.fromToken, state.toToken)
        val signature = state.transaction.signature
        setSuccessState(message, signature)
    }

    private fun handleSendSuccess(state: TransactionState.SendSuccess) {
        val message = getString(R.string.send_successfully_format, state.sourceTokenSymbol)
        val signature = state.transaction.signature
        setSuccessState(message, signature)
    }

    private fun setSuccessState(message: String, signature: String) {
        with(binding) {
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_succeeded))
            progressStateTransaction.setSuccessState()
            progressStateTransaction.setDescriptionText(R.string.transaction_description_succeeded)
            buttonDone.setText(R.string.common_done)
        }
    }

    private fun handleProgress(state: TransactionState.Progress) {
        with(binding) {
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_submitted))
            progressStateTransaction.setDescriptionText(R.string.transaction_description_progress)
            buttonDone.setText(R.string.common_done)
        }
    }

    private fun handleError(state: TransactionState.Error) {
        with(binding) {
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_failed))
            progressStateTransaction.setFailedState()
            progressStateTransaction.setDescriptionText(R.string.transaction_description_failed)
            buttonDone.setText(R.string.common_close)
        }
    }
}
