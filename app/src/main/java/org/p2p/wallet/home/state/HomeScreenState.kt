package org.p2p.wallet.home.state

import java.math.BigDecimal
import org.p2p.core.utils.emptyString
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton

data class HomeScreenState(
    val solanaTokens: State.SolTokens = State.SolTokens(tokens = emptyList()),
    val ethTokens: State.EthTokens = State.EthTokens(tokens = emptyList(),),
    val strigaTokens: State.StrigaTokens = State.StrigaTokens(emptyList()),
    val strigaBanner: State.StrigaStatusBanner? = null,
    val actionButtons: List<ActionButton> = emptyList(),
    val isRefreshing: Boolean = false,
    val username: String = emptyString(),
    val userBalance: BigDecimal? = null
)
