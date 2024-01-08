package org.p2p.wallet.jupiter.ui.main

import java.math.BigDecimal
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.token.service.model.TokenRate
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.jupiter.interactor.JupiterSwapInteractor
import org.p2p.wallet.jupiter.interactor.JupiterSwapTokensResult
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.user.repository.UserLocalRepository

class JupiterTestPresenterBuilder {

    var swapOpenedFrom: SwapOpenedFrom = SwapOpenedFrom.MAIN_SCREEN
    var initialAmountA: String? = null
    var initialTokenAMint: Base58String? = null
    var initialTokenBMint: Base58String? = null
    var preinstallTokenA: Token.Active? = null

    /**
     * All tokens. Also used for UserLocalRepository
     * @see HomeLocalRepository.getTokensFlow()
     * @see UserLocalRepository
     */
    var homeRepoAllTokens: MutableList<Token.Active> = mutableListOf()

    /**
     * Tokens on user's balance
     * @see HomeLocalRepository.getUserTokens()
     */
    var homeRepoUserTokens: MutableList<Token.Active> = mutableListOf()

    /**
     * Mock for
     * @see JupiterSwapRoutesRepository.getSwappableTokenMints(Base58String)
     */
    var jupiterSwapRoutesRepoGetSwappableTokenMints: (Base58String) -> List<Base58String> = {
        JupiterSwapTestHelpers.DEFAULT_SWAPPABLE_TOKENS
    }

    /**
     * Mock for
     * @see JupiterSwapRoutesRepository.getSwapRoutesForSwapPair(JupiterSwapPair, Base58String)
     */
    var jupiterSwapRoutesRepoGetSwapRoutesForSwapPair: (JupiterSwapPair, Base58String) -> List<JupiterSwapRoute> = { pair, pk ->
        listOf(
            JupiterSwapTestHelpers.createSwapRoute(
                TestSwapRouteData(pair, pk)
            )
        )
    }

    /**
     * Mock for
     * @see JupiterSwapTokensRepository.getTokens()
     */
    var jupiterSwapTokensRepoGetTokens: MutableList<JupiterSwapToken> = mutableListOf(
        JupiterSwapTestHelpers.JUPITER_SOL_TOKEN,
        JupiterSwapTestHelpers.JUPITER_USDC_TOKEN
    )

    /**
     * Mock for
     * @see JupiterSwapTokensRepository.getTokenRate(JupiterSwapToken)
     */
    var jupiterSwapTokensRepoGetTokenRate: (JupiterSwapToken) -> TokenServicePrice? = { token ->
        when (token) {
            JupiterSwapTestHelpers.JUPITER_SOL_TOKEN -> {
                TokenServicePrice(token.coingeckoId!!, TokenRate(BigDecimal.TEN), network = TokenServiceNetwork.SOLANA)
            }

            JupiterSwapTestHelpers.JUPITER_USDC_TOKEN -> {
                TokenServicePrice(token.coingeckoId!!, TokenRate(BigDecimal.ONE), network = TokenServiceNetwork.SOLANA)
            }

            else -> null
        }
    }

    /**
     * Mock for
     * @see JupiterSwapTokensRepository.loadTokensRate(List<JupiterSwapToken>)
     */
    var jupiterSwapTokensRepoGetTokensRate: (List<JupiterSwapToken>) -> Map<Base58String, TokenServicePrice> = { tokens ->

        tokens.mapNotNull { token ->
            when (token) {
                JupiterSwapTestHelpers.JUPITER_SOL_TOKEN -> {
                    TokenServicePrice(token.coingeckoId!!, TokenRate(BigDecimal.TEN), network = TokenServiceNetwork.SOLANA)
                }

                JupiterSwapTestHelpers.JUPITER_USDC_TOKEN -> {
                    TokenServicePrice(token.coingeckoId!!, TokenRate(BigDecimal.ONE), network = TokenServiceNetwork.SOLANA)
                }

                else -> null
            }
        }.associateBy { it.address.toBase58Instance() }
    }

    /**
     * Mock for
     * @see JupiterSwapInteractor.swapTokens(Base64String)
     */
    var jupiterSwapInteractorSwapTokens: (Base64String) -> JupiterSwapTokensResult = { _ ->
        JupiterSwapTokensResult.Success("signature")
    }
}
