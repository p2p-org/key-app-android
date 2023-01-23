package org.p2p.wallet.moonpay.ui.transaction

import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellCancelResult
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import timber.log.Timber
import kotlinx.coroutines.launch

class SellTransactionDetailsPresenter(
    private val currentTransaction: SellTransactionViewDetails,
    private val sellInteractor: SellInteractor
) : BasePresenter<SellTransactionDetailsContract.View>(),
    SellTransactionDetailsContract.Presenter {

    override fun onCancelTransactionClicked() {
        launch {
            when (val result = sellInteractor.cancelTransaction(currentTransaction.transactionId)) {
                is MoonpaySellCancelResult.CancelSuccess -> {
                    view?.close()
                }
                is MoonpaySellCancelResult.CancelFailed -> {
                    Timber.e(result.cause, "Failed to cancel transaction")
                    view?.showUiKitSnackBar(messageResId = R.string.sell_details_cancel_failed)
                }
            }
        }
    }

    override fun onRemoveFromHistoryClicked() {
        sellInteractor.hideTransactionFromHistory(currentTransaction.transactionId)
        view?.close()
    }
}
