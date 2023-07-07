package org.p2p.wallet.home.events

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.token.Token
import org.p2p.core.utils.emptyString
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.wallet.interactor.StrigaClaimInteractor
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken
import org.p2p.wallet.user.interactor.UserInteractor

class HomeScreenStateLoader(
    private val userInteractor: UserInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val strigaClaimInteractor: StrigaClaimInteractor,
    private val homeInteractor: HomeInteractor,
    private val appScope: AppScope
) : AppLoader {

    override suspend fun onLoad() {

        appScope.launch {
            userInteractor.getUserTokensFlow()
                .combine(ethereumInteractor.observeTokensFlow()) { solTokens, ethTokens ->
                    Timber.tag("_______").d("Solana changed = $solTokens")
                    Timber.tag("_______").d("Eth changed = $ethTokens")
                    val strigaTokens = getStrigaClaimableTokens()
                    HomeScreenState(
                        solanaTokens = State.SolTokens(solTokens, isLoading = false),
                        ethTokens = State.EthTokens(ethTokens, isLoading = false),
                        strigaTokens = State.StrigaTokens(strigaTokens)
                    )
                }
                .combine(homeInteractor.observeActionButtons()) { homeScreenState, actionButtons ->
                    Timber.tag("_______").d("buttons changed = $actionButtons")
                    homeScreenState.copy(actionButtons = actionButtons)
                }
                .combine(homeInteractor.observeUserBalance()) { homeScreenState, userBalance ->
                    Timber.tag("_______").d("balance changed = $userBalance")
                    homeScreenState.copy(userBalance = userBalance)
                }
                .combine(homeInteractor.observeRefreshState()) { tokensState, isRefreshing ->
                    Timber.tag("_______").d("refresh changed = $isRefreshing")
                    tokensState.copy(isRefreshing = isRefreshing)
                }.distinctUntilChanged()
                .collect {
                    homeInteractor.updateHomeScreenState(it)
                }
        }
    }

    override suspend fun onRefresh() {
        onLoad()
    }

    private suspend fun getStrigaClaimableTokens(): List<StrigaClaimableToken> {
        return strigaClaimInteractor.getClaimableTokens().successOrNull().orEmpty()
    }

    sealed interface State {
        data class SolTokens(
            val tokens: List<Token.Active> = emptyList(),
            val isLoading: Boolean = true
        ) : State

        data class EthTokens(
            val tokens: List<Token.Eth> = emptyList(),
            val isLoading: Boolean = true
        ) : State

        data class StrigaTokens(val tokens: List<StrigaClaimableToken>) : State
        data class StrigaStatusBanner(val banner: StrigaKycStatusBanner) : State
    }

    data class HomeScreenState(
        val solanaTokens: State.SolTokens = State.SolTokens(tokens = emptyList(), isLoading = true),
        val ethTokens: State.EthTokens = State.EthTokens(tokens = emptyList(), isLoading = true),
        val strigaTokens: State.StrigaTokens = State.StrigaTokens(emptyList()),
        val strigaBanner: State.StrigaStatusBanner? = null,
        val actionButtons: List<ActionButton> = emptyList(),
        val isRefreshing: Boolean = false,
        val username: String = emptyString(),
        val userBalance: BigDecimal? = null
    )
}
