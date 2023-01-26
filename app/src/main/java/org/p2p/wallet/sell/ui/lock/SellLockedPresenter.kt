package org.p2p.wallet.sell.ui.lock

import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellCancelResult
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber
import kotlinx.coroutines.launch

class SellLockedPresenter(
    private val currentTransaction: SellTransactionViewDetails,
    private val sellInteractor: SellInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<SellLockedContract.View>(),
    SellLockedContract.Presenter {

    override fun onCancelTransactionClicked() {
        launch {
            when (val result = sellInteractor.cancelTransaction(currentTransaction.transactionId)) {
                is MoonpaySellCancelResult.CancelSuccess -> {
                    view?.navigateBack()
                }
                is MoonpaySellCancelResult.CancelFailed -> {
                    Timber.e(result.cause, "Failed to cancel transaction")
                    view?.showUiKitSnackBar(messageResId = R.string.sell_details_cancel_failed)
                    view?.navigateBackToMain()
                }
            }
        }
    }

    override fun onSendClicked() {
        launch {
            val solToken = userInteractor.getUserSolToken() ?: return@launch
            view?.navigateToSendScreen(
                tokenToSend = solToken,
                sendAmount = currentTransaction.formattedSolAmount.toBigDecimal(),
                receiverAddress = currentTransaction.receiverAddress
            )
        }
    }
}
