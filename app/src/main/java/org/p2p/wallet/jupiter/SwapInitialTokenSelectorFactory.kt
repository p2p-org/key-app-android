package org.p2p.wallet.jupiter

import org.koin.core.module.dsl.new
import org.koin.core.scope.Scope
import org.p2p.wallet.jupiter.statemanager.token_selector.CommonSwapTokenSelector
import org.p2p.wallet.jupiter.statemanager.token_selector.PreinstallTokenASelector
import org.p2p.wallet.jupiter.statemanager.token_selector.PreinstallTokensBySymbolSelector
import org.p2p.wallet.jupiter.statemanager.token_selector.SwapInitialTokenSelector
import org.p2p.wallet.jupiter.statemanager.token_selector.SwapInitialTokensData

object SwapInitialTokenSelectorFactory {
    fun create(koinScope: Scope, initialSwapData: SwapInitialTokensData): SwapInitialTokenSelector =
        with(koinScope) {
            val tokenASymbol = initialSwapData.tokenASymbol
            val tokenBSymbol = initialSwapData.tokenBSymbol
            val initialToken = initialSwapData.token
            when {
                !tokenASymbol.isNullOrBlank() && !tokenBSymbol.isNullOrBlank() -> {
                    PreinstallTokensBySymbolSelector(
                        jupiterTokensRepository = get(),
                        dispatchers = get(),
                        homeLocalRepository = get(),
                        savedSelectedSwapTokenStorage = get(),
                        preinstallTokenASymbol = initialSwapData.tokenASymbol,
                        preinstallTokenBSymbol = initialSwapData.tokenBSymbol,
                    )
                }
                initialToken != null -> {
                    PreinstallTokenASelector(
                        jupiterTokensRepository = get(),
                        dispatchers = get(),
                        homeLocalRepository = get(),
                        savedSelectedSwapTokenStorage = get(),
                        preinstallTokenA = initialSwapData.token,
                    )
                }
                else -> {
                    new(::CommonSwapTokenSelector)
                }
            }
        }
}
