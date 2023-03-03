package org.p2p.wallet.swap.ui.jupiter.tokens.presenter

import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel

data class SwapTokensCellModelPayload(
    val hasPopularLabel: Boolean,
    val isSearchResultItem: Boolean,
    val tokenModel: SwapTokenModel
)
