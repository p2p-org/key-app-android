package org.p2p.wallet.history.ui.history

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentHistoryBinding
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetFragment
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.moonpay.ui.transaction.SellTransactionDetailsBottomSheet
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class HistoryFragment :
    BaseMvpFragment<HistoryContract.View, HistoryContract.Presenter>(R.layout.fragment_history),
    HistoryContract.View {

    companion object {
        fun create() = HistoryFragment()
    }

    override val presenter: HistoryContract.Presenter by inject()
    private val binding: FragmentHistoryBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.layoutHistoryList.apply {
            bind(
                onTransactionClicked = presenter::onTransactionClicked,
                onSellTransactionClicked = presenter::onSellTransactionClicked,
                mintAddress = Constants.WRAPPED_SOL_MINT
            )
        }
        listenForSellTransactionDialogDismiss()
    }

    private fun listenForSellTransactionDialogDismiss() {
        childFragmentManager.setFragmentResultListener(
            SellTransactionDetailsBottomSheet.REQUEST_KEY_DISMISSED, this
        ) { _, _ -> binding.layoutHistoryList.loadHistory() }
    }

    override fun showBuyScreen(token: Token) {
        replaceFragment(NewBuyFragment.create(token))
    }

    override fun openTransactionDetailsScreen(transactionId: String) {
        HistoryTransactionDetailsBottomSheetFragment.show(
            fragmentManager = parentFragmentManager,
            transactionId = transactionId
        )
    }

    override fun openSellTransactionDetails(transactionId: String) {
        SellTransactionDetailsBottomSheet.show(childFragmentManager, transactionId)
    }
}
