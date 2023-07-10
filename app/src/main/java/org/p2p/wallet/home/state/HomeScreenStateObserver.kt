package org.p2p.wallet.home.state

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.token.Token
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaClaimInteractor
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken
import org.p2p.wallet.user.interactor.UserInteractor

class HomeScreenStateObserver(
    private val userInteractor: UserInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val strigaClaimInteractor: StrigaClaimInteractor,
    private val homeInteractor: HomeInteractor,
    private val appScope: AppScope,
    private val analytics: HomeAnalytics
) {

    private var observeJob: Job? = null

    fun start() {
        observeJob = appScope.launch {
            userInteractor.getUserTokensFlow().onEach {
                Timber.tag("_______").d("Solana changed = $it")
            }
                .combine(ethereumInteractor.observeTokensFlow()) { solTokens, ethTokens ->
                    Timber.tag("_______").d("Eth changed = $ethTokens")
                    val strigaTokens = getStrigaClaimableTokens()
                    HomeScreenState(
                        solanaTokens = State.SolTokens(solTokens),
                        ethTokens = State.EthTokens(ethTokens),
                        strigaTokens = State.StrigaTokens(strigaTokens),
                        userBalance = getUserBalance(solTokens)
                    )
                }
                .shareIn(scope = this, started = SharingStarted.WhileSubscribed(), replay = 0).collect { homeState ->
                    Timber.tag("_______").d("Home state =$homeState")
                    homeInteractor.updateHomeScreenState(homeState)
                }
        }
    }

    private suspend fun getStrigaClaimableTokens(): List<StrigaClaimableToken> {
        return strigaClaimInteractor.getClaimableTokens().successOrNull().orEmpty()
    }

    private fun getUserBalance(tokens: List<Token.Active>): BigDecimal? {
        if (tokens.none { it.totalInUsd != null }) return null
        return tokens
            .mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()
            .also(::logBalance)
    }

    private fun logBalance(balance: BigDecimal?) {
        val hasPositiveBalance = balance != null && balance.isMoreThan(BigDecimal.ZERO)
        analytics.logUserHasPositiveBalanceProperty(hasPositiveBalance)
        analytics.logUserAggregateBalanceProperty(balance.orZero())
    }
}
