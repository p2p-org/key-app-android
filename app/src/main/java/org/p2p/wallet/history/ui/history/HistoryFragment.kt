package org.p2p.wallet.history.ui.history

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentHistoryBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetFragment
import org.p2p.wallet.history.ui.historylist.HistoryListViewContract
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.moonpay.ui.transaction.SellTransactionDetailsBottomSheet
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
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

    private val historyListViewPresenter: HistoryListViewContract.Presenter by inject { parametersOf(null) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.layoutHistoryList.apply {
            bind(
                historyListViewPresenter = historyListViewPresenter,
                onBuyClicked = presenter::onBuyClicked,
                onReceiveClicked = { replaceFragment(ReceiveSolanaFragment.create(token = null)) },
                onTransactionClicked = presenter::onTransactionClicked,
                onSellTransactionClicked = presenter::onSellTransactionClicked
            )
            addObserver(lifecycle)
        }

        listenForSellTransactionDialogDismiss()
        lifecycle.addObserver(presenter)
    }

    private fun listenForSellTransactionDialogDismiss() {
        childFragmentManager.setFragmentResultListener(
            SellTransactionDetailsBottomSheet.REQUEST_KEY_DISMISSED, this
        ) { _, _ -> binding.layoutHistoryList.loadHistory() }
    }

    override fun showBuyScreen(token: Token) {
        replaceFragment(NewBuyFragment.create(token))
    }

    override fun openTransactionDetailsScreen(transaction: HistoryTransaction) {
        when (transaction) {
            is RpcHistoryTransaction.Swap,
            is RpcHistoryTransaction.Transfer,
            is RpcHistoryTransaction.BurnOrMint -> {
                val transaction = transaction as RpcHistoryTransaction
                HistoryTransactionDetailsBottomSheetFragment.show(
                    fragmentManager = parentFragmentManager,
                    signature = transaction.signature
                )
            }
            else -> {
                Timber.e(IllegalArgumentException("Unsupported transaction type: $transaction"))
            }
        }
    }

    override fun openSellTransactionDetails(sellTransaction: SellTransactionViewDetails) {
        SellTransactionDetailsBottomSheet.show(childFragmentManager, sellTransaction)
    }
}
