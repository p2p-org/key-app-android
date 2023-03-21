package org.p2p.wallet.jupiter.ui.settings.presenter

import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.jupiter.repository.model.JupiterSwapMarketInformation
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.model.findTokenByMint
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.utils.Base58String

class SwapFeeLoader(
    private val swapTokensRepository: JupiterSwapTokensRepository,
    private val dispatchers: CoroutineDispatchers,
) {

    suspend fun getAccountFee(
        activeRoute: JupiterSwapRoute?,
        solToken: JupiterSwapToken?,
    ): SwapSettingFeeBox? {
        if (solToken == null) {
            return null
        }
        val feeAmount = activeRoute?.fees
            ?.totalFeeAndDepositsInSol
            ?.fromLamports(solToken.decimals)
            ?: return null

        val solTokenRate = swapTokensRepository.getTokenRate(solToken)?.price ?: return null
        val feeUsd: BigDecimal = feeAmount.multiply(solTokenRate)

        return SwapSettingFeeBox(
            amountLamports = feeAmount,
            amountUsd = feeUsd,
            token = solToken
        )
    }

    suspend fun getLiquidityFeeList(
        activeRoute: JupiterSwapRoute?,
        jupiterTokens: List<JupiterSwapToken>,
    ): List<SwapSettingFeeBox>? = withContext(dispatchers.io) {
        activeRoute ?: return@withContext null

        val tokenMintByRate = getLiqidityTokensRates(activeRoute, jupiterTokens)

        activeRoute.marketInfos.mapNotNull { marketInfo ->
            createLiquidityFeeBox(marketInfo, jupiterTokens, tokenMintByRate)
        }
    }

    private suspend fun getLiqidityTokensRates(
        activeRoute: JupiterSwapRoute,
        jupiterTokens: List<JupiterSwapToken>
    ): Map<Base58String, TokenPrice> {
        val lpTokens = activeRoute.marketInfos.mapNotNull { marketInfo ->
            val lpFee = marketInfo.liquidityFee
            jupiterTokens.findTokenByMint(lpFee.mint)
        }
            .distinctBy(JupiterSwapToken::tokenMint)

        return swapTokensRepository.getTokensRates(lpTokens)
    }

    private fun createLiquidityFeeBox(
        marketInfo: JupiterSwapMarketInformation,
        jupiterTokens: List<JupiterSwapToken>,
        tokenMintByRate: Map<Base58String, TokenPrice>
    ): SwapSettingFeeBox? {
        val lpFee = marketInfo.liquidityFee
        val lpToken = jupiterTokens.findTokenByMint(lpFee.mint) ?: return null
        val tokeRateDetails = tokenMintByRate[lpFee.mint] ?: return null

        val amountLamports = lpFee.amountInLamports.fromLamports(lpToken.decimals)
        val amountUsd = amountLamports.multiply(tokeRateDetails.price)

        return SwapSettingFeeBox(
            amountLamports = amountLamports,
            amountUsd = amountUsd,
            token = lpToken
        )
    }
}

data class SwapSettingFeeBox(
    val amountLamports: BigDecimal,
    val amountUsd: BigDecimal,
    val token: JupiterSwapToken,
)
