package org.p2p.wallet.solend.ui.earn

import android.content.Context
import androidx.annotation.CallSuper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.earnwidget.EarnWidgetState
import org.p2p.wallet.solend.interactor.SolendDepositsInteractor
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.orZero
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

private val COLLATERAL_ACCOUNTS = listOf("SOL", "USDT", "USDC", "BTC", "ETH")

class SolendEarnPresenter(
    private val context: Context,
    private val solendDepositsInteractor: SolendDepositsInteractor,
    private val depositTickerManager: DepositTickerManager
) : BasePresenter<SolendEarnContract.View>(), SolendEarnContract.Presenter {

    private var timerJob: Job? = null
    private var lastDepositTickerBalance: BigDecimal = depositTickerManager.getTickerBalance(BigDecimal.ZERO)
    private var blockedErrorState = true

    private var deposits by Delegates.observable(emptyList<SolendDepositToken>()) { _, _, newValue ->
        view?.showAvailableDeposits(newValue)
    }

    override fun attach(view: SolendEarnContract.View) {
        super.attach(view)
        handleDepositsResult(deposits)
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
                handleDepositsResult(result)
            } catch (e: Throwable) {
                Timber.e(e, "Error fetching available deposit tokens")
                showDepositsWidgetError()
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
                saveLastDepositTicker()
                handleDepositsResult(result)
            } catch (e: Throwable) {
                Timber.e(e, "Error fetching available deposit tokens")
                showDepositsWidgetError()
                view?.showErrorSnackBar(e.getErrorMessage(context))
            } finally {
                view?.showRefreshing(isRefreshing = false)
            }
        }
    }

    override fun onDepositTokenClicked(deposit: SolendDepositToken) {
        if (!blockedErrorState) {
            view?.showDepositTopUp(deposit)
        }
    }

    private fun showDepositsWidgetError() {
        timerJob?.cancel()
        blockedErrorState = true
        view?.showWidgetState(
            EarnWidgetState.Error(
                messageTextRes = R.string.earn_widget_error_message_show_info,
                buttonTextRes = R.string.earn_widget_error_button_try_again,
            )
        )
        view?.bindWidgetActionButton { refresh() }
    }

    private fun handleDepositsResult(newDeposits: List<SolendDepositToken>) {
        deposits = newDeposits

        when {
            newDeposits.sumOf { (it as? SolendDepositToken.Active)?.usdAmount.orZero() } == BigDecimal.ZERO -> {
                view?.showWidgetState(EarnWidgetState.LearnMore)
            }
            newDeposits.any { it.supplyInterest == null } -> {
                showDepositsWidgetError()
                view?.setRatesErrorVisibility(isVisible = true)
            }
            newDeposits.any { it is SolendDepositToken.Active } -> {
                val activeDeposits = newDeposits.filterIsInstance<SolendDepositToken.Active>()
                val depositBalance = activeDeposits.sumOf { it.usdAmount }
                val totalYearBalance = activeDeposits.sumOf {
                    it.usdAmount / BigDecimal(100) * (it.supplyInterest ?: BigDecimal.ZERO)
                }
                val tokenIcons = activeDeposits.map { it.iconUrl.orEmpty() }

                val tickerAmount = depositTickerManager.getTickerBalance(depositBalance)
                view?.showWidgetState(EarnWidgetState.Balance(tickerAmount, tokenIcons))
                startBalanceTicker(tickerAmount, totalYearBalance, tokenIcons)

                view?.bindWidgetActionButton { view?.navigateToUserDeposits(activeDeposits) }
                view?.setRatesErrorVisibility(isVisible = false)
                blockedErrorState = false
            }
            else -> {
                view?.showWidgetState(EarnWidgetState.Idle)
            }
        }
    }

    @CallSuper
    override fun detach() {
        saveLastDepositTicker()
        super.detach()
    }

    private fun saveLastDepositTicker() {
        if (lastDepositTickerBalance != BigDecimal.ZERO) {
            depositTickerManager.setLastTickerBalance(lastDepositTickerBalance)
        }
    }

    private fun startBalanceTicker(
        initialBalance: BigDecimal,
        totalYearBalance: BigDecimal,
        tokenIconsUrls: List<String>
    ) {
        val delta = (initialBalance - totalYearBalance) / BigDecimal(TimeUnit.DAYS.toSeconds(365))
        timerJob?.cancel()
        val timer = (1..Int.MAX_VALUE)
            .asSequence()
            .asFlow()
            .onEach { delay(1_000) }

        timerJob = launch() {
            lastDepositTickerBalance = initialBalance
            timer.collect {
                lastDepositTickerBalance += delta
                view?.showWidgetState(EarnWidgetState.Balance(lastDepositTickerBalance, tokenIconsUrls))
            }
        }
    }
}
