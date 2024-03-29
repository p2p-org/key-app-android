package org.p2p.wallet.jupiter.ui.main

import java.math.BigDecimal
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel

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
