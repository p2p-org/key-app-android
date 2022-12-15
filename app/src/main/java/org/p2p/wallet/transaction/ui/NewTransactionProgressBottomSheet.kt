package org.p2p.wallet.transaction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogNewTransactionProgressBinding
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

/**
 * Bottom sheet dialog which shows current transaction's state
 * The default state when it's launched is [TransactionState.Progress]
 * */

private const val EXTRA_DATA = "EXTRA_DATA"
private const val EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID"

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

    private val binding: DialogNewTransactionProgressBinding by viewBinding()

    private val data: NewShowProgress by args(EXTRA_DATA)
    private val transactionId: String by args(EXTRA_TRANSACTION_ID)

    private lateinit var progressStateFormat: String

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_new_transaction_progress, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressStateFormat = getString(R.string.transaction_progress_title)
        with(binding) {
            buttonDone.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }

        observeState()
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
            titleTextView.text = message
            progressBar.isVisible = false
            progressStateTransaction.setSuccessState()
            progressStateTransaction.setDescriptionText(R.string.transaction_description_succeeded)

            transactionIdGroup.isVisible = true
            transactionIdTextView.text = signature
            transactionLabelTextView.setOnClickListener {
                val solanaUrl = getString(R.string.solanaExplorer, signature)
                showUrlInCustomTabs(solanaUrl)
            }

            transactionImageView.setImageResource(R.drawable.ic_success)
            lineView.isVisible = true
            lineView.setBackgroundColor(getColor(R.color.systemSuccessMain))
            buttonDone.setText(R.string.common_done)
        }
    }

    private fun handleProgress(state: TransactionState.Progress) {
        with(binding) {
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_submitted))
            progressBar.isVisible = true

            transactionIdGroup.isVisible = true
            transactionIdTextView.text = getString(R.string.common_commas)
            progressStateTransaction.setDescriptionText(R.string.transaction_description_progress)

            transactionImageView.setImageResource(R.drawable.ic_pending)
            lineView.isVisible = false
            buttonDone.setText(R.string.common_done)
        }
    }

    private fun handleError(state: TransactionState.Error) {
        with(binding) {
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_failed))
            titleTextView.text = state.message
            progressBar.isVisible = false
            progressStateTransaction.setFailedState()
            progressStateTransaction.setDescriptionText(R.string.transaction_description_failed)

            transactionImageView.setImageResource(R.drawable.ic_error_transaction)
            lineView.isVisible = true
            lineView.setBackgroundColor(getColor(R.color.systemErrorMain))
            buttonDone.setText(R.string.common_close)
        }
    }
}
