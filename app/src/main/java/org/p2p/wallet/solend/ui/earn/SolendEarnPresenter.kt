package org.p2p.wallet.solend.ui.earn

import android.content.Context
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.interactor.SolendDepositsInteractor
import org.p2p.wallet.utils.getErrorMessage
import timber.log.Timber
import kotlinx.coroutines.launch

private val COLLATERAL_ACCOUNTS = listOf("SOL", "USDT", "USDC", "BTC", "ETH")

class SolendEarnPresenter(
    private val context: Context,
    private val solendDepositsInteractor: SolendDepositsInteractor
) : BasePresenter<SolendEarnContract.View>(), SolendEarnContract.Presenter {

    override fun load() {
        view?.showLoading(isLoading = true)
        launch {
            try {
                val deposits = solendDepositsInteractor.getUserDeposits(COLLATERAL_ACCOUNTS)
                view?.showDeposits(deposits)
            } catch (e: Throwable) {
                Timber.e(e, "Error fetching available deposit tokens")
                view?.showErrorSnackBar(e.getErrorMessage(context))
            } finally {
                view?.showLoading(isLoading = false)
            }
        }
    }

    override fun refresh() {
        view?.showRefreshing(isRefreshing = true)
        launch {
            try {
                val deposits = solendDepositsInteractor.getUserDeposits(COLLATERAL_ACCOUNTS)
                view?.showDeposits(deposits)
            } catch (e: Throwable) {
                Timber.e(e, "Error fetching available deposit tokens")
                view?.showErrorSnackBar(e.getErrorMessage(context))
            } finally {
                view?.showRefreshing(isRefreshing = false)
            }
        }
    }
}
