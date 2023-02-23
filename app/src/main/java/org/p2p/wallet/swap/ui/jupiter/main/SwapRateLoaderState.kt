package org.p2p.wallet.swap.ui.jupiter.main

import java.math.BigDecimal
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel

sealed interface SwapRateLoaderState {
    object Empty : SwapRateLoaderState
    object Loading : SwapRateLoaderState
    object Error : SwapRateLoaderState

    data class Loaded(
        val token: SwapTokenModel,
        val rate: BigDecimal,
    ) : SwapRateLoaderState

    data class NoRateAvailable(
        val token: SwapTokenModel,
    ) : SwapRateLoaderState
}
