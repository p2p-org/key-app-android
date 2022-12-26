package org.p2p.wallet.swap.model.orca

import org.p2p.core.token.Token
import org.p2p.wallet.swap.model.Slippage

class SwapState(
    private var inputAmount: String = "0",
    private var destinationAmount: String = "0",
    private var slippage: Slippage = Slippage.TopUpSlippage,
    private var bestPoolPair: OrcaPoolsPair? = null,
    private var sourceToken: Token.Active,
    private var destinationToken: Token? = null
)
