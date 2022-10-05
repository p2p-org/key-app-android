package org.p2p.wallet.solend.ui.earn

import android.content.Context
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.earnwidget.EarnWidgetState
import org.p2p.wallet.solend.interactor.SolendDepositsInteractor
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.getErrorMessage
import timber.log.Timber
import kotlin.properties.Delegates

private val COLLATERAL_ACCOUNTS = listOf("SOL", "USDT", "USDC", "BTC", "ETH")

class SolendEarnPresenter(
    private val context: Context,
    private val solendDepositsInteractor: SolendDepositsInteractor
) : BasePresenter<SolendEarnContract.View>(), SolendEarnContract.Presenter {

    private var deposits by Delegates.observable(emptyList<SolendDepositToken>()) { _, _, newValue ->
        view?.showAvailableDeposits(newValue)
    }

    override fun attach(view: SolendEarnContract.View) {
        super.attach(view)
        handleResult(deposits)
    }

    override fun load() {
        if (deposits.isNotEmpty()) {
            view?.showLoading(isLoading = false)
            return
        }

        view?.showLoading(isLoading = true)
        launch {
            try {
                val result = solendDepositsInteractor.getUserDeposits(COLLATERAL_ACCOUNTS)
                handleResult(result)
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
                val result = solendDepositsInteractor.getUserDeposits(COLLATERAL_ACCOUNTS)
                handleResult(result)
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

    private fun handleResult(result: List<SolendDepositToken>) {
        deposits = result

        when {
            result.any { it is SolendDepositToken.Active } -> {
                val activeDeposits = result.filterIsInstance<SolendDepositToken.Active>()
                val deposits = activeDeposits.sumOf { it.usdAmount }
                val tokenIcons = activeDeposits.map { it.iconUrl.orEmpty() }
                view?.showWidgetState(EarnWidgetState.Balance(deposits, tokenIcons))
                view?.bindWidgetActionButton { view?.navigateToUserDeposits(activeDeposits) }
            }
            else -> {
                // TODO: add further states
                view?.showWidgetState(EarnWidgetState.LearnMore)
            }
        }
    }
}
