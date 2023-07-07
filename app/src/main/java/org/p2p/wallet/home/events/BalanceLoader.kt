package org.p2p.wallet.home.events

import java.math.BigDecimal
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.token.Token
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.ui.main.HomeInteractor

class BalanceLoader(
    private val appScope: AppScope,
    private val analytics: HomeAnalytics,
    private val homeInteractor: HomeInteractor
) : AppLoader {

    override suspend fun onLoad() {
        appScope.launch {
            homeInteractor.observeHomeScreenState()
                .filterNotNull()
                .distinctUntilChanged()
                .collect { homeState ->
                    val solTokens = homeState.solanaTokens.tokens
                    val balance = getUserBalance(solTokens)
                    val hasPositiveBalance = balance != null && balance.isMoreThan(BigDecimal.ZERO)

                    analytics.logUserHasPositiveBalanceProperty(hasPositiveBalance)
                    analytics.logUserAggregateBalanceProperty(balance.orZero())
                    homeInteractor.updateUserBalance(balance.orZero())
                }
        }
    }

    private fun getUserBalance(tokens: List<Token.Active>): BigDecimal? {
        if (tokens.none { it.totalInUsd != null }) return null
        return tokens
            .mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()
    }
}
