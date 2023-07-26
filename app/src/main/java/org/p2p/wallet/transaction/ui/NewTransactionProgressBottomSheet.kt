package org.p2p.wallet.transaction.ui

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogNewTransactionProgressBinding
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.progressstate.TransactionState
import org.p2p.wallet.transaction.progresshandler.TransactionProgressHandler
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

/**
 * Bottom sheet dialog which shows current transaction's state
 * The default state when it's launched is [TransactionState.Progress]
 * */

private const val EXTRA_DATA = "EXTRA_DATA"
private const val EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID"
private const val EXTRA_HANDLER_QUALIFIER = "EXTRA_HANDLER_QUALIFIER"

class NewTransactionProgressBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun show(
            fragmentManager: FragmentManager,
            transactionId: String,
            data: NewShowProgress,
            handlerQualifierName: String
        ) {
            NewTransactionProgressBottomSheet()
                .withArgs(
                    EXTRA_DATA to data,
                    EXTRA_TRANSACTION_ID to transactionId,
                    EXTRA_HANDLER_QUALIFIER to handlerQualifierName,
                )
                .show(fragmentManager, NewTransactionProgressBottomSheet::javaClass.name)
        }

        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(NewTransactionProgressBottomSheet::javaClass.name)
            (dialog as? NewTransactionProgressBottomSheet)?.dismissAllowingStateLoss()
        }
    }

    private val handlerQualifier: String by args(EXTRA_HANDLER_QUALIFIER)
    private val progressStateHandler: TransactionProgressHandler by lazy {
        get(named(handlerQualifier))
    }

    private val transactionManager: TransactionManager by inject()

    private val binding: DialogNewTransactionProgressBinding by viewBinding()

    private val data: NewShowProgress by args(EXTRA_DATA)
    private val transactionId: String by args(EXTRA_TRANSACTION_ID)

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_new_transaction_progress, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressStateHandler.init(binding, data)
        binding.buttonDone.setOnClickListener {
            dismissAllowingStateLoss()
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
                progressStateHandler.handleState(state)
            }
        }
    }
}
