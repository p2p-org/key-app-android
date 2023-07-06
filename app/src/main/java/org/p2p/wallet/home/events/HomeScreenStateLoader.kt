package org.p2p.wallet.home.events

import java.math.BigDecimal
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
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
) : HomeScreenLoader {

    override suspend fun onLoad() {
        userInteractor.getUserTokensFlow()
            .combine(ethereumInteractor.observeTokensFlow()) { solTokens, ethTokens ->
                val strigaTokens = getStrigaClaimableTokens()
                HomeScreenState(
                    tokenStates = listOf(
                        State.SolTokens(solTokens),
                        State.EthTokens(ethTokens),
                        State.StrigaTokens(strigaTokens)
                    )
                )
            }
            .combine(homeInteractor.getUserStatusBannerFlow().filterNotNull()) { homeScreenState, strigaBanner ->
                homeScreenState.copy(strigaBanner = State.StrigaStatusBanner(strigaBanner))
            }
            .combine(homeInteractor.observeActionButtons()) { homeScreenState, actionButtons ->
                homeScreenState.copy(actionButtons = actionButtons)
            }
            .combine(homeInteractor.observeUserBalance()) { homeScreenState, userBalance ->
                homeScreenState.copy(userBalance = userBalance)
            }
            .combine(homeInteractor.observeRefreshState()) { tokensState, isRefreshing ->
                val state = tokensState.copy(isRefreshing = isRefreshing)
                homeInteractor.updateHomeScreenState(state)
            }
    }

    override suspend fun onRefresh(): Unit = Unit

    private suspend fun getStrigaClaimableTokens(): List<StrigaClaimableToken> {
        return strigaClaimInteractor.getClaimableTokens().successOrNull().orEmpty()
    }

    sealed interface State {
        data class SolTokens(val tokens: List<Token.Active>) : State
        data class EthTokens(val tokens: List<Token.Eth>) : State
        data class StrigaTokens(val tokens: List<StrigaClaimableToken>) : State
        data class StrigaStatusBanner(val banner: StrigaKycStatusBanner) : State
    }

    data class HomeScreenState(
        val tokenStates: List<State> = emptyList(),
        val strigaBanner: State.StrigaStatusBanner? = null,
        val actionButtons: List<ActionButton> = emptyList(),
        val isRefreshing: Boolean = false,
        val username: String = emptyString(),
        val userBalance: BigDecimal? = null
    )
}
