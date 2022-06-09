package org.p2p.wallet.history.ui.detailsbottomsheet

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.date.toDateTimeString
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import timber.log.Timber

class TransactionDetailsBottomSheetPresenter(
    private val state: TransactionDetailsLaunchState,
    private val historyInteractor: HistoryInteractor
) : BasePresenter<TransactionDetailsBottomSheetContract.View>(),
    TransactionDetailsBottomSheetContract.Presenter {

    override fun attach(view: TransactionDetailsBottomSheetContract.View) {
        super.attach(view)
        load()
    }

    override fun load() {
        when (state) {
            is TransactionDetailsLaunchState.History -> handleHistory(state.transaction)
            is TransactionDetailsLaunchState.Id -> handleId(state)
        }
    }

    private fun handleId(state: TransactionDetailsLaunchState.Id) {
        launch {
            try {
                view?.showLoading(true)

                val details = historyInteractor.getHistoryTransaction(state.tokenPublicKey)

                if (details != null) {
                    handleHistory(details)
                } else {
                    view?.showError(R.string.error_general_message)
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error loading transaction details")
                view?.showError(R.string.details_transaction_not_found)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun handleHistory(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap -> parseSwap(transaction)
            is HistoryTransaction.Transfer -> parseTransfer(transaction)
            is HistoryTransaction.BurnOrMint -> parseBurnOrMint(transaction)
            else -> Timber.e("Unsupported transaction: $transaction")
        }
    }

    private fun parseSwap(transaction: HistoryTransaction.Swap) {
        view?.apply {
            showDate(transaction.date.toDateTimeString())
            showStatus(transaction.status)

            val usdTotal = transaction.getReceivedUsdAmount()
            val total = transaction.getFormattedAmount()
            showSignature(transaction.signature)
            showAddresses(transaction.sourceAddress, transaction.destinationAddress)
            showAmount(total, usdTotal)
            showFee()
            showBlockNumber(transaction.getBlockNumber())
            showSwapView(transaction.sourceIconUrl, transaction.destinationIconUrl)
        }
    }

    private fun parseTransfer(transaction: HistoryTransaction.Transfer) {
        view?.apply {
            showDate(transaction.date.toDateTimeString())
            showStatus(transaction.status)

            showSignature(transaction.signature)
            showAddresses(transaction.senderAddress, transaction.destination)

            val usdTotal = transaction.getFormattedAmount()
            val total = transaction.getFormattedTotal()
            showAmount(total, usdTotal)
            showFee()
            showBlockNumber(transaction.getBlockNumber())
            showTransferView(transaction.getIcon())
        }
    }

    private fun parseBurnOrMint(transaction: HistoryTransaction.BurnOrMint) {
        view?.apply {
            showDate(transaction.date.toDateTimeString())

            showSignature(transaction.signature)
            showAddresses(transaction.senderAddress, transaction.destination)

            val usdTotal = transaction.getFormattedAmount()
            val total = transaction.getFormattedTotal()
            showAmount(total, usdTotal)
            showFee()
            showBlockNumber(transaction.getBlockNumber())
            showTransferView(transaction.getIcon())
        }
    }
}
