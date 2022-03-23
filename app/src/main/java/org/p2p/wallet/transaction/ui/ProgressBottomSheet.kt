package org.p2p.wallet.transaction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import org.p2p.wallet.databinding.DialogTransactionProgressBinding
import org.p2p.wallet.transaction.TransactionManager
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getColor
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

/**
 * Bottom sheet dialog which shows current transaction's state
 * The default state when it's launched is [TransactionState.Progress]
 * */

private const val EXTRA_DATA = "EXTRA_DATA"
private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
const val EXTRA_RESULT_KEY_DISMISS = "EXTRA_RESULT_KEY_DISMISS"

class ProgressBottomSheet : NonDraggableBottomSheetDialogFragment() {

    companion object {
        fun show(fragmentManager: FragmentManager, data: ShowProgress, requestKey: String) {
            ProgressBottomSheet()
                .withArgs(
                    EXTRA_DATA to data,
                    EXTRA_REQUEST_KEY to requestKey
                )
                .show(fragmentManager, ProgressBottomSheet::javaClass.name)
        }

        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(ProgressBottomSheet::javaClass.name)
            (dialog as? ProgressBottomSheet)?.dismissAllowingStateLoss()
        }
    }

    private val transactionManager: TransactionManager by inject()

    private val binding: DialogTransactionProgressBinding by viewBinding()

    private val data: ShowProgress by args(EXTRA_DATA)
    private val requestKey: String by args(EXTRA_REQUEST_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_transaction_progress, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            subTitleTextView.text = data.subTitle
            transactionIdGroup.isVisible = data.transactionId.isNotEmpty()

            transactionLabelTextView.setOnClickListener {
                val url = getString(R.string.solanaExplorer, data.transactionId)
                showUrlInCustomTabs(url)
            }

            transactionIdTextView.text = data.transactionId

            arrowImageView.setOnClickListener {
                setResultAndDismiss()
            }
            doneButton.setOnClickListener {
                setResultAndDismiss()
            }
        }

        observeState()
    }

    override fun onStop() {
        lifecycleScope.launch {
            // clearing state after transaction is viewed
            transactionManager.emitTransactionState(TransactionState.Progress())
        }
        super.onStop()
    }

    private fun observeState() {
        lifecycleScope.launchWhenResumed {
            transactionManager.getTransactionStateFlow().collect { state ->
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
            titleTextView.text = message
            progressBar.isVisible = false

            transactionIdGroup.isVisible = true
            transactionIdTextView.text = signature

            transactionImageView.setImageResource(R.drawable.ic_success)
            lineView.isVisible = true
            lineView.setBackgroundColor(getColor(R.color.systemSuccessMain))
        }
    }

    private fun handleProgress(state: TransactionState.Progress) {
        with(binding) {
            titleTextView.setText(state.message)
            progressBar.isVisible = true

            transactionIdGroup.isVisible = true
            transactionIdTextView.text = getString(R.string.common_commas)

            transactionImageView.setImageResource(R.drawable.ic_pending)
            lineView.isVisible = false
        }
    }

    private fun handleError(state: TransactionState.Error) {
        with(binding) {
            titleTextView.text = state.message
            progressBar.isVisible = false

            transactionImageView.setImageResource(R.drawable.ic_error_transaction)
            lineView.isVisible = true
            lineView.setBackgroundColor(getColor(R.color.systemErrorMain))
        }
    }

    private fun setResultAndDismiss() {
        setFragmentResult(requestKey, bundleOf(EXTRA_RESULT_KEY_DISMISS to null))
        dismissAllowingStateLoss()
    }
}
