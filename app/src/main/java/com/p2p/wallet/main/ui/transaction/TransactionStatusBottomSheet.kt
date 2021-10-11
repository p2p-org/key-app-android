package com.p2p.wallet.main.ui.transaction

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.p2p.wallet.R
import com.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import com.p2p.wallet.databinding.DialogStatusBottomSheetBinding
import com.p2p.wallet.utils.showUrlInCustomTabs
import com.p2p.wallet.utils.viewbinding.viewBinding

class TransactionStatusBottomSheet(
    private val onDismiss: (() -> Unit)?
) : NonDraggableBottomSheetDialogFragment() {

    companion object {

        fun show(
            activity: FragmentActivity,
            info: TransactionInfo,
            onDismiss: (() -> Unit)? = null
        ) {
            show(activity.supportFragmentManager, info, onDismiss)
        }

        fun show(
            fragment: Fragment,
            info: TransactionInfo,
            onDismiss: (() -> Unit)? = null
        ) {
            show(fragment.childFragmentManager, info, onDismiss)
        }

        fun show(
            fragmentManager: FragmentManager,
            info: TransactionInfo,
            onDismiss: (() -> Unit)?
        ) {
            TransactionStatusBottomSheet(onDismiss)
                .apply { information = info }
                .show(fragmentManager, TransactionStatusBottomSheet::javaClass.name)
        }
    }

    private val binding: DialogStatusBottomSheetBinding by viewBinding()

    private var information: TransactionInfo? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_status_bottom_sheet, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val info = information ?: return dismissAllowingStateLoss()
        with(binding) {
            statusTextView.text = getString(info.status)
            messageTextView.text = getString(info.message)
            iconImageView.setImageResource(info.iconRes)
            amountTextView.text = "${info.amount} ${info.tokenSymbol}"
            usdAmountTextView.text = getString(R.string.main_usd_end_format, info.usdAmount)
            idTextView.text = info.transactionId

            viewImageView.setOnClickListener {
                val url = getString(R.string.solanaExplorer, info.transactionId)
                showUrlInCustomTabs(url)
            }

            doneButton.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDismiss?.invoke()
        super.onDismiss(dialog)
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}