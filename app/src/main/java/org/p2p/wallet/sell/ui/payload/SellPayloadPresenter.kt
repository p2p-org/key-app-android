package org.p2p.wallet.sell.ui.payload

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber
import java.math.BigDecimal

private val MIN_AMOUNT_TO_SELL = BigDecimal.valueOf(20)
private val MAX_AMOUNT_TO_SELL = BigDecimal.valueOf(100)
class SellPayloadPresenter(
    private val sellInteractor: SellInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<SellPayloadContract.View>(),
    SellPayloadContract.Presenter {

    override fun load() {
        launch {
            try {
                view?.showLoading(isVisible = true)
                if (isUserHasTransactionsInProcess()) {
                    view?.navigateToSellLock()
                    return@launch
                }
                initView()
            } catch (e: Throwable) {
                Timber.e("Error on init view $e")
            } finally {
                view?.showLoading(isVisible = false)
            }
        }
    }

    private suspend fun isUserHasTransactionsInProcess(): Boolean {
        val userTransactions = sellInteractor.loadUserSellTransactions()
        return userTransactions.isNotEmpty() && userTransactions.all {
            it.status == MoonpaySellTransaction.TransactionStatus.COMPLETED
        }
    }

    private suspend fun initView() {
        val solToken = userInteractor.getUserSolToken() ?: return
        view?.showAvailableSolToSell(solToken.total)
        view?.setMinSolToSell(MIN_AMOUNT_TO_SELL, solToken.tokenSymbol.uppercase())
    }

    override fun cashOut() {}
}
