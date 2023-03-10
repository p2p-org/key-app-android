package org.p2p.wallet.jupiter.ui.tokens.presenter

import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel

data class SwapTokensCellModelPayload(
    val hasPopularLabel: Boolean,
    val isSearchResultItem: Boolean,
    val tokenModel: SwapTokenModel
)
