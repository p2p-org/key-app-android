package org.p2p.wallet.jupiter

import org.koin.core.module.dsl.new
import org.koin.core.scope.Scope
import org.p2p.wallet.jupiter.statemanager.token_selector.CommonSwapTokenSelector
import org.p2p.wallet.jupiter.statemanager.token_selector.PreinstallTokenASelector
import org.p2p.wallet.jupiter.statemanager.token_selector.PreinstallTokensByMintSelector
import org.p2p.wallet.jupiter.statemanager.token_selector.SwapInitialTokenSelector
import org.p2p.wallet.jupiter.statemanager.token_selector.SwapInitialTokensData

object SwapInitialTokenSelectorFactory {
    fun create(koinScope: Scope, initialSwapData: SwapInitialTokensData): SwapInitialTokenSelector =
        with(koinScope) {
            val tokenAMint = initialSwapData.tokenAMint?.base58Value
            val tokenBMint = initialSwapData.tokenBMint?.base58Value
            val initialToken = initialSwapData.token
            when {
                !tokenAMint.isNullOrBlank() && !tokenBMint.isNullOrBlank() -> {
                    PreinstallTokensByMintSelector(
                        jupiterTokensRepository = get(),
                        dispatchers = get(),
                        tokenServiceCoordinator = get(),
                        savedSelectedSwapTokenStorage = get(),
                        preinstallTokenAMint = initialSwapData.tokenAMint,
                        preinstallTokenBMint = initialSwapData.tokenBMint,
                    )
                }
                initialToken != null -> {
                    PreinstallTokenASelector(
                        jupiterTokensRepository = get(),
                        dispatchers = get(),
                        tokenServiceCoordinator = get(),
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
