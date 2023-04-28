package org.p2p.wallet.jupiter.ui.main

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.utils.Base58String

class JupiterTestPresenterBuilder {

    var swapOpenedFrom: SwapOpenedFrom = SwapOpenedFrom.MAIN_SCREEN
    var initialAmountA: String? = null
    var initialTokenASymbol: String? = null
    var initialTokenBSymbol: String? = null
    var preinstallTokenA: Token.Active? = null

    var homeRepoAllTokens: MutableList<Token.Active> = mutableListOf()
    var homeRepoUserTokens: MutableList<Token.Active> = mutableListOf()

    var jupiterSwapSwappableTokenMintsGetter: (Base58String) -> List<Base58String> = {
        JupiterSwapTestHelpers.DEFAULT_SWAPPABLE_TOKENS
    }
    var jupiterSwapRoutesGetter: (JupiterSwapPair, Base58String) -> List<JupiterSwapRoute> = { pair, pk ->
        listOf(
            JupiterSwapTestHelpers.createSwapRoute(
                JupiterSwapTestHelpers.TestSwapRouteData(pair, pk)
            )
        )
    }

    var jupiterRepoTokens: MutableList<JupiterSwapToken> = mutableListOf(
        JupiterSwapTestHelpers.JUPITER_SOL_TOKEN,
        JupiterSwapTestHelpers.JUPITER_USDC_TOKEN
    )
    var jupiterTokenRepoGetTokenRate: (JupiterSwapToken) -> TokenPrice? = { token ->
        when (token) {
            JupiterSwapTestHelpers.JUPITER_SOL_TOKEN -> {
                TokenPrice(token.coingeckoId!!, BigDecimal("100"))
            }

            JupiterSwapTestHelpers.JUPITER_USDC_TOKEN -> {
                TokenPrice(token.coingeckoId!!, BigDecimal("1"))
            }

            else -> null
        }
    }
    var jupiterTokenRepoGetTokensRate: (List<JupiterSwapToken>) -> Map<Base58String, TokenPrice> = { tokens ->
        tokens.mapNotNull { token ->
            when (token) {
                JupiterSwapTestHelpers.JUPITER_SOL_TOKEN -> {
                    token.tokenMint to TokenPrice(token.coingeckoId!!, BigDecimal("100"))
                }

                JupiterSwapTestHelpers.JUPITER_USDC_TOKEN -> {
                    token.tokenMint to TokenPrice(token.coingeckoId!!, BigDecimal("1"))
                }

                else -> null
            }
        }.toMap()
    }
}
