package org.p2p.wallet.history.ui.detailsbottomsheet

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.username.repository.model.UsernameDetails
import org.p2p.wallet.common.date.toDateTimeString
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction

class HistoryTransactionDetailsBottomSheetPresenter(
    private val historyInteractor: HistoryInteractor,
    private val usernameInteractor: UsernameInteractor
) : BasePresenter<HistoryTransactionDetailsContract.View>(),
    HistoryTransactionDetailsContract.Presenter {

    override fun attach(view: HistoryTransactionDetailsContract.View) {
        super.attach(view)
        load()
    }

    override fun load() {
        launch {
            view?.showLoading(isLoading = true)
            // TODO provide loading method
            view?.showLoading(isLoading = false)
        }
    }

    private suspend fun loadDetailsByTransaction(transaction: RpcHistoryTransaction) {
        when (transaction) {
            is RpcHistoryTransaction.Swap -> {
                parseSwap(transaction)
            }
            is RpcHistoryTransaction.Transfer -> {
                parseTransfer(transaction)
            }
            is RpcHistoryTransaction.BurnOrMint -> {
                parseBurnOrMint(transaction)
            }
            else -> {
                Timber.e("Unsupported transaction: $transaction")
                view?.showError(R.string.details_transaction_not_found)
            }
        }
    }

    private fun parseSwap(transaction: RpcHistoryTransaction.Swap) {
        view?.apply {
            showDate(transaction.date.toDateTimeString())
            showStatus(transaction.status)

            showTransactionId(transaction.signature)
            showAddresses(transaction.sourceAddress, transaction.destinationAddress)

            val usdTotal = transaction.getReceivedUsdAmount()
            val total = transaction.getFormattedAmount()
            showAmount(total, usdTotal)
            showFee()

            showSwapView(transaction.sourceIconUrl, transaction.destinationIconUrl)
        }
    }

    private suspend fun parseTransfer(transaction: RpcHistoryTransaction.Transfer) {
        view?.apply {
            showTransferView(transaction.getIcon())
            showAmount(
                amountToken = transaction.getFormattedTotal(),
                amountUsd = transaction.getFormattedAmount()
            )
            showDate(transaction.date.toDateTimeString())
            showTransferAddress(
                isSend = transaction.isSend,
                senderAddress = transaction.senderAddress,
                receiverAddress = transaction.destination
            )
            showFee()
            showStatus(transaction.status)
            showTransactionId(transaction.signature)
        }
    }

    private suspend fun showTransferAddress(isSend: Boolean, senderAddress: String, receiverAddress: String) {
        val transferActorAddress = if (isSend) receiverAddress.toBase58Instance() else senderAddress.toBase58Instance()
        val transferActorUsername: UsernameDetails? = getTransferActorUsername(transferActorAddress)
        if (isSend) {
            view?.showReceiverAddress(
                receiverAddress = transferActorAddress,
                receiverUsername = transferActorUsername?.username?.fullUsername
            )
        } else {
            view?.showSenderAddress(
                senderAddress = transferActorAddress,
                senderUsername = transferActorUsername?.username?.fullUsername
            )
        }
    }

    private suspend fun getTransferActorUsername(actorAddress: Base58String): UsernameDetails? {
        return runCatching { usernameInteractor.findUsernameByAddress(actorAddress) }
            .onFailure { Timber.e(it) }
            .getOrNull()
    }

    private fun parseBurnOrMint(transaction: RpcHistoryTransaction.BurnOrMint) {
        view?.apply {
            showDate(transaction.date.toDateTimeString())

            showTransactionId(transaction.signature)
            showAddresses(transaction.senderAddress, transaction.destination)

            val usdTotal = transaction.getFormattedAmount()
            val total = transaction.getFormattedTotal()
            showAmount(total, usdTotal)
            showFee()
            showTransferView(transaction.getIcon())
        }
    }
}
