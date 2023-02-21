package org.p2p.wallet.swap.jupiter.statemanager.token_selector

import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel

interface InitialTokenSelector {

    suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel>
}
