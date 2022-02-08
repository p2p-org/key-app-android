package org.p2p.wallet.transaction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import org.p2p.wallet.databinding.DialogTransactionProgressBinding
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class ProgressBottomSheet : NonDraggableBottomSheetDialogFragment() {

    companion object {
        private const val EXTRA_DATA = "EXTRA_DATA"
        fun show(fragmentManager: FragmentManager, data: ShowProgress) {
            ProgressBottomSheet()
                .withArgs(EXTRA_DATA to data)
                .show(fragmentManager, ProgressBottomSheet::javaClass.name)
        }

        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(ProgressBottomSheet::javaClass.name)
            if (dialog is ProgressBottomSheet) {
                dialog.dismissAllowingStateLoss()
            }
        }
    }

    private val sendInteractor: SendInteractor by inject()

    private val binding: DialogTransactionProgressBinding by viewBinding()

    private val data: ShowProgress by args(EXTRA_DATA)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_transaction_progress, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            lifecycleScope.launchWhenResumed {
                sendInteractor.getTransactionIdFlow().collect {
                    transactionIdGroup.isVisible = true
                    transactionIdTextView.text = it.cutEnd()
                }
            }

            subTitleTextView.text = data.subTitle

            transactionIdGroup.isVisible = data.transactionId.isNotEmpty()
            transactionLabelTextView.setOnClickListener {
                val url = getString(R.string.solanaExplorer, data.transactionId)
                showUrlInCustomTabs(url)
            }
            transactionIdTextView.text = data.transactionId
            showButton.setOnClickListener {
                data.onPrimaryCallback()
                dismissAllowingStateLoss()
            }

            secondaryButton.setOnClickListener {
                data.onSecondaryCallback()
                dismissAllowingStateLoss()
            }
        }
    }
}