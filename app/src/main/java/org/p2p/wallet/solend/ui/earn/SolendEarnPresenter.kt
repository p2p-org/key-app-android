package org.p2p.wallet.solend.ui.earn

import android.content.Context
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.interactor.SolendDepositsInteractor
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.getErrorMessage
import timber.log.Timber

private val COLLATERAL_ACCOUNTS = listOf("SOL", "USDT", "USDC", "BTC", "ETH")

class SolendEarnPresenter(
    private val context: Context,
    private val solendDepositsInteractor: SolendDepositsInteractor
) : BasePresenter<SolendEarnContract.View>(), SolendEarnContract.Presenter {

    private var cachedDeposits: List<SolendDepositToken> = emptyList()

    override fun load() {
        if (cachedDeposits.isNotEmpty()) {
            showDeposits(cachedDeposits)
            view?.showLoading(isLoading = false)
            refresh()
        } else {
            view?.showLoading(isLoading = true)
            launch {
                try {
                    showDeposits(solendDepositsInteractor.getUserDeposits(COLLATERAL_ACCOUNTS))
                } catch (e: Throwable) {
                    Timber.e(e, "Error fetching available deposit tokens")
                    view?.showErrorSnackBar(e.getErrorMessage(context))
                } finally {
                    view?.showLoading(isLoading = false)
                }
            }
        }
    }

    override fun refresh() {
        view?.showRefreshing(isRefreshing = true)
        launch {
            try {
                showDeposits(solendDepositsInteractor.getUserDeposits(COLLATERAL_ACCOUNTS))
            } catch (e: Throwable) {
                Timber.e(e, "Error fetching available deposit tokens")
                view?.showErrorSnackBar(e.getErrorMessage(context))
            } finally {
                view?.showRefreshing(isRefreshing = false)
            }
        }
    }

    override fun onDepositTokenClicked(deposit: SolendDepositToken) {
        view?.showDepositTopUp(deposit)
    }

    private fun showDeposits(deposits: List<SolendDepositToken>) {
        cachedDeposits = deposits
        view?.showDeposits(deposits)
    }
}
