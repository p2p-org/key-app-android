package org.p2p.wallet.history.ui.history

import android.content.Context
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentHistoryBinding
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetFragment
import org.p2p.wallet.history.ui.historylist.HistoryListViewClickListener
import org.p2p.wallet.history.ui.historylist.HistoryListViewContract
import org.p2p.wallet.history.ui.historylist.HistoryListViewType
import org.p2p.wallet.history.ui.sendvialink.HistorySendLinksFragment
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.moonpay.ui.transaction.SellTransactionDetailsBottomSheet
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.progresshandler.SendSwapTransactionProgressHandler
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class HistoryFragment :
    BaseMvpFragment<HistoryContract.View, HistoryContract.Presenter>(R.layout.fragment_history),
    HistoryContract.View,
    HistoryListViewClickListener {

    companion object {
        fun create(): HistoryFragment = HistoryFragment()
    }

    override val presenter: HistoryContract.Presenter by inject()

    /**
     * We need to attach this presenter to the HistoryFragment lifecycle,
     * as it should survive the destruction of HistoryListView.
     * Otherwise, the presenter will be initialized again and the history list will be reloaded
     * every time we return to the HistoryFragment from the backstack.
     */
    private val historyListPresenter: HistoryListViewContract.Presenter by inject()
    private val binding: FragmentHistoryBinding by viewBinding()

    private var listener: RootListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.layoutHistoryList.bind(
            presenter = historyListPresenter,
            clickListener = this,
            listType = HistoryListViewType.AllHistory
        )
        listenForSellTransactionDialogDismiss()
    }

    override fun onUserSendLinksClicked() {
        replaceFragment(HistorySendLinksFragment.create())
    }

    override fun onTransactionClicked(transactionId: String) {
        presenter.onTransactionClicked(transactionId)
    }

    override fun onSellTransactionClicked(transactionId: String) {
        presenter.onSellTransactionClicked(transactionId)
    }

    override fun onSwapBannerClicked(
        sourceTokenMint: String,
        destinationTokenMint: String,
        sourceSymbol: String,
        destinationSymbol: String,
        openedFrom: SwapOpenedFrom
    ) {
        replaceFragment(
            JupiterSwapFragment.create(
                tokenAMint = sourceTokenMint.toBase58Instance(),
                tokenBMint = destinationTokenMint.toBase58Instance(),
                amountA = Constants.ZERO_AMOUNT,
                source = openedFrom,
            )
        )
    }

    override fun onBridgeSendClicked(transactionId: String) {
        presenter.onSendPendingClicked(transactionId)
    }

    override fun onBridgeClaimClicked(transactionId: String) {
        presenter.onClaimPendingClicked(transactionId)
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

    override fun showProgressDialog(bundleId: String, progressDetails: NewShowProgress) {
        listener?.showTransactionProgress(bundleId, progressDetails, SendSwapTransactionProgressHandler.QUALIFIER)
    }
}
