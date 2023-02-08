package org.p2p.wallet.solend.ui.earn

import androidx.annotation.CallSuper
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.earnwidget.EarnWidgetState
import org.p2p.wallet.infrastructure.account.AccountStorage
import org.p2p.wallet.infrastructure.account.AccountStorageContract
import org.p2p.wallet.infrastructure.account.AccountStorageContract.Key.Companion.withCustomKey
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.solend.interactor.SolendDepositInteractor
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SolendEarnPresenter(
    private val resourcesProvider: ResourcesProvider,
    private val solendDepositsInteractor: SolendDepositInteractor,
    private val userInteractor: UserInteractor,
    private val depositTickerStorage: DepositTickerStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val accountStorage: AccountStorage
) : BasePresenter<SolendEarnContract.View>(), SolendEarnContract.Presenter {

    private var timerJob: Job? = null
    private var lastDepositTickerBalance: BigDecimal = depositTickerStorage.getTickerBalance(BigDecimal.ZERO)
    private var blockedErrorState = true
    private var onboardingFirstShowing = true

    private var deposits by Delegates.observable(emptyList<SolendDepositToken>()) { _, _, newValue ->
        view?.showAvailableDeposits(newValue)
    }

    override fun attach(view: SolendEarnContract.View) {
        super.attach(view)
        launch {
            val onboardingCompletedKey = accountStorage.getString(
                AccountStorageContract.Key.KEY_SOLEND_ONBOARDING_COMPLETED.withCustomKey(
                    tokenKeyProvider.publicKey
                )
            )
            val needShowOnboarding = onboardingCompletedKey == null
            if (needShowOnboarding && onboardingFirstShowing) {
                onboardingFirstShowing = false
                view.showSolendOnboarding()
            }
            if (deposits.isNotEmpty()) {
                handleDepositsResult(deposits)
            }
        }
    }

    override fun load() {
        if (deposits.isNotEmpty()) {
            view?.showLoading(isLoading = false)
            return
        }

        view?.showLoading(isLoading = true)
        view?.showWidgetState(EarnWidgetState.Idle)
        launch {
            try {
                val result = solendDepositsInteractor.getUserDeposits()
                handleDepositsResult(result)
            } catch (e: Throwable) {
                Timber.e(e, "Error fetching available deposit tokens")
                showDepositsWidgetError()
                view?.showUiKitSnackBar(e.getErrorMessage { res -> resourcesProvider.getString(res) })
            } finally {
                view?.showLoading(isLoading = false)
            }
        }
    }

    override fun refresh() {
        view?.showRefreshing(isRefreshing = true)
        launch {
            try {
                val result = solendDepositsInteractor.getUserDeposits()
                saveLastDepositTicker()
                handleDepositsResult(result)
            } catch (e: Throwable) {
                Timber.e(e, "Error fetching available deposit tokens")
                showDepositsWidgetError()
                view?.showUiKitSnackBar(e.getErrorMessage { res -> resourcesProvider.getString(res) })
            } finally {
                view?.showRefreshing(isRefreshing = false)
            }
        }
    }

    override fun onDepositTokenClicked(deposit: SolendDepositToken) {
        if (!blockedErrorState) {
            launch {
                val activeToken = userInteractor.getUserTokens().firstOrNull {
                    it.tokenSymbol == deposit.tokenSymbol
                }
                if (activeToken?.totalInUsd.orZero().isZero()) {
                    view?.showDepositTopUp(deposit)
                } else {
                    view?.showDepositToSolend(deposit, deposits)
                }
            }
        }
    }

    override fun resetTickerBalance() {
        lastDepositTickerBalance = BigDecimal.ZERO
        depositTickerStorage.setLastTickerBalance(lastDepositTickerBalance)
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
        deposits = newDeposits.sortedByDescending { it.supplyInterest }

        when {
            newDeposits.sumOf { (it as? SolendDepositToken.Active)?.usdAmount.orZero() } == BigDecimal.ZERO -> {
                view?.showWidgetState(EarnWidgetState.LearnMore)
                view?.bindWidgetActionButton { view?.navigateToFaq() }
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

                val tickerAmount = depositTickerStorage.getTickerBalance(depositBalance)
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
            depositTickerStorage.setLastTickerBalance(lastDepositTickerBalance)
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
