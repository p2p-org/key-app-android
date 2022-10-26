package org.p2p.wallet.solend.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogSolendTransactionDetailsPartBinding
import org.p2p.wallet.solend.model.SolendTransactionDetailsState
import org.p2p.wallet.solend.model.SolendTransactionDetails
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private const val ARG_TRANSACTION_STATE = "ARG_TRANSACTION_STATE"

class TransactionDetailsBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String,
            state: SolendTransactionDetailsState,
            requestKey: String = ARG_REQUEST_KEY,
            resultKey: String = ARG_RESULT_KEY
        ) = TransactionDetailsBottomSheet().withArgs(
            ARG_TITLE to title,
            ARG_TRANSACTION_STATE to state,
            ARG_REQUEST_KEY to requestKey,
            ARG_RESULT_KEY to resultKey
        ).show(fm, TransactionDetailsBottomSheet::javaClass.name)
    }

    private val state: SolendTransactionDetailsState by args(ARG_TRANSACTION_STATE)

    private lateinit var binding: DialogSolendTransactionDetailsPartBinding

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSolendTransactionDetailsPartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDoneButtonVisibility(isVisible = false)
        setCloseButtonVisibility(isVisible = true)
        binding.textViewFreeFee.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.solend_transaction_free_fee_title)
                .setMessage(R.string.solend_transaction_free_fee_message)
                .setPositiveButton(R.string.solend_transaction_free_fee_button, null)
                .show()
        }
        when (val state = state) {
            is SolendTransactionDetailsState.Deposit -> showDepositData(state.deposit)
            is SolendTransactionDetailsState.Withdraw -> showWithdrawData(state.withdraw)
        }
    }

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(requireView().parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    private fun showDepositData(viewData: SolendTransactionDetails) = with(binding) {
        optionsTextViewTransaction.labelText = getString(R.string.solend_transaction_details_deposit)
        optionsTextViewTransactionFee.labelText = getString(R.string.solend_transaction_details_deposit_fee)

        setViewData(viewData)
    }

    private fun showWithdrawData(viewData: SolendTransactionDetails) = with(binding) {
        optionsTextViewTransaction.labelText = getString(R.string.solend_transaction_details_withdraw)
        optionsTextViewTransactionFee.labelText = getString(R.string.solend_transaction_details_withdrawal_fee)

        setViewData(viewData)
    }

    private fun setViewData(viewData: SolendTransactionDetails) = with(binding) {
        with(viewData) {
            optionsTextViewTransaction.setValueText(amount)
            textViewFreeFee.isVisible = transferFee == null
            optionsTextViewTransferFee.setValueText(transferFee.orEmpty())
            optionsTextViewTransactionFee.setValueText(fee)
            optionsTextViewTotal.setValueText(total)
        }
        overrideColors()
    }

    private fun overrideColors() = with(binding) {
        optionsTextViewTransaction.setLabelTextColor(getColor(R.color.text_mountain))
        optionsTextViewTransferFee.setLabelTextColor(getColor(R.color.text_mountain))
        optionsTextViewTransactionFee.setLabelTextColor(getColor(R.color.text_mountain))
        optionsTextViewTotal.setLabelTextColor(getColor(R.color.text_mountain))
    }
}
