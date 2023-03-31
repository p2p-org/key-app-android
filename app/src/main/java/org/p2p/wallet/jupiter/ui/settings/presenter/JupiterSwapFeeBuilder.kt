package org.p2p.wallet.jupiter.ui.settings.presenter

import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.model.findTokenByMint
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository

class JupiterSwapFeeBuilder(
    private val swapTokensRepository: JupiterSwapTokensRepository,
    private val dispatchers: CoroutineDispatchers,
) {

    suspend fun buildAccountFeeBox(
        activeRoute: JupiterSwapRoute?,
        solToken: JupiterSwapToken?,
    ): SwapSettingFeeBox? {
        solToken ?: return null

        val feeAmountLamports: BigDecimal =
            activeRoute?.fees
                ?.totalFeeAndDepositsInSol
                ?.fromLamports(solToken.decimals)
                ?: return null

        val solTokenRate: BigDecimal? = swapTokensRepository.getTokenRate(solToken)?.price

        val feeUsd: BigDecimal? =
            solTokenRate?.let { feeAmountLamports.multiply(it) }

        return SwapSettingFeeBox(
            amountLamports = feeAmountLamports,
            amountUsd = feeUsd,
            token = solToken
        )
    }

    suspend fun buildLiquidityFeeListBox(
        activeRoute: JupiterSwapRoute?,
        jupiterTokens: List<JupiterSwapToken>,
    ): List<SwapSettingFeeBox>? = withContext(dispatchers.io) {
        activeRoute ?: return@withContext null

        val lpTokensRates = activeRoute.marketInfos.map { marketInfo ->
            val lpFee = marketInfo.liquidityFee
            jupiterTokens.findTokenByMint(lpFee.mint) ?: return@withContext null
        }
            .distinctBy { it.tokenMint }
            .let { swapTokensRepository.getTokensRates(it) }

        activeRoute.marketInfos.map { marketInfo ->
            val lpFee = marketInfo.liquidityFee
            val lpToken = jupiterTokens.findTokenByMint(lpFee.mint) ?: return@withContext null
            val tokenRate: BigDecimal? = lpTokensRates[lpFee.mint]?.price
            val amountLamports = lpFee.amountInLamports.fromLamports(lpToken.decimals)
            val amountUsd = tokenRate?.let { amountLamports.multiply(it) }

            SwapSettingFeeBox(
                amountLamports = amountLamports,
                amountUsd = amountUsd,
                token = lpToken
            )
        }
    }
}

data class SwapSettingFeeBox(
    val amountLamports: BigDecimal,
    val amountUsd: BigDecimal?,
    val token: JupiterSwapToken,
)
