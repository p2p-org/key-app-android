package org.p2p.wallet.history.ui.token

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails

class TokenHistoryPresenter(
    private val token: Token.Active,
    private val historyAnalytics: HistoryAnalytics,
    private val renBtcInteractor: RenBtcInteractor,
    private val tokenInteractor: TokenInteractor,
) : BasePresenter<TokenHistoryContract.View>(), TokenHistoryContract.Presenter {

    override fun attach(view: TokenHistoryContract.View) {
        super.attach(view)
        initialize()
    }

    private fun initialize() {
        val actionButtons = mutableListOf(
            ActionButton.RECEIVE_BUTTON,
            ActionButton.SEND_BUTTON,
            ActionButton.SWAP_BUTTON
        )

        if (token.isSOL || token.isUSDC) {
            actionButtons.add(0, ActionButton.BUY_BUTTON)
        }

        view?.showActionButtons(actionButtons)
    }

    override fun onItemClicked(historyItem: HistoryItem) {
        when (historyItem) {
            is HistoryItem.TransactionItem -> onTransactionItemClicked(historyItem.transaction)
            is HistoryItem.MoonpayTransactionItem -> onSellTransactionClicked(historyItem.transactionDetails)
            else -> {
                val errorMessage = "Unsupported Transaction click! $historyItem"
                Timber.e(errorMessage)
                throw UnsupportedOperationException(errorMessage)
            }
        }
    }

    private fun onTransactionItemClicked(transaction: HistoryTransaction) {
        logTransactionClicked(transaction)
        view?.showDetailsScreen(transaction)
    }

    private fun logTransactionClicked(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap -> {
                historyAnalytics.logSwapTransactionClicked(transaction)
            }
            is HistoryTransaction.Transfer -> {
                launch {
                    historyAnalytics.logTransferTransactionClicked(
                        transaction = transaction,
                        isRenBtcSessionActive = renBtcInteractor.isUserHasActiveSession()
                    )
                }
            }
            else -> Unit // log other types later
        }
    }

    private fun onSellTransactionClicked(sellTransaction: SellTransactionViewDetails) {
        historyAnalytics.logSellTransactionClicked(sellTransaction)
        view?.openSellTransactionDetails(sellTransaction)
    }

    override fun closeAccount() {
        launch {
            try {
                tokenInteractor.closeTokenAccount(token.publicKey)
                view?.showUiKitSnackBar(messageResId = R.string.details_account_closed_successfully)
            } catch (e: Throwable) {
                Timber.e(e, "Error closing account: ${token.publicKey}")
                view?.showErrorMessage(e)
            }
        }
    }
}
