package org.p2p.wallet.jupiter.ui.settings.presenter

import java.math.BigDecimal
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.model.findTokenByMint
import org.p2p.wallet.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.jupiter.ui.main.SwapRateLoaderState

class SwapFeeLoader(
    private val swapStateManager: SwapStateManager,
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

        val solTokenRate = swapStateManager.getTokenRate(SwapTokenModel.JupiterToken(solToken))
            .filterIsInstance<SwapRateLoaderState.Loaded>()
            .firstOrNull()
            ?: return null

        val feeUsd: BigDecimal = feeAmount.multiply(solTokenRate.rate)
        return SwapSettingFeeBox(feeAmount, feeUsd, solTokenRate.token)
    }

    suspend fun getLiquidityFeeList(
        activeRoute: JupiterSwapRoute?,
        jupiterTokens: List<JupiterSwapToken>,
    ): List<SwapSettingFeeBox>? = withContext(dispatchers.io) {
        activeRoute ?: return@withContext null
        val rateLoaderList = activeRoute.marketInfos.map { marketInfo ->
            val lpFee = marketInfo.liquidityFee
            val lpToken = jupiterTokens.findTokenByMint(lpFee.mint) ?: return@withContext null
            async {
                swapStateManager.getTokenRate(SwapTokenModel.JupiterToken(lpToken))
                    .filterIsInstance<SwapRateLoaderState.Loaded>()
                    .map { it }
                    .firstOrNull()
            }
        }
        val lpRateLoader = rateLoaderList.awaitAll()

        activeRoute.marketInfos.map { marketInfo ->
            val lpFee = marketInfo.liquidityFee
            val lpToken = jupiterTokens.findTokenByMint(lpFee.mint) ?: return@withContext null
            val rateToke = lpRateLoader
                .find { it?.token?.mintAddress == lpFee.mint } ?: return@withContext null
            val amountLamports = lpFee.amountInLamports
                .fromLamports(lpToken.decimals)
            val amountUsd = amountLamports.multiply(rateToke.rate)
            SwapSettingFeeBox(amountLamports, amountUsd, rateToke.token)
        }
    }
}

data class SwapSettingFeeBox(
    val amountLamports: BigDecimal,
    val amountUsd: BigDecimal,
    val token: SwapTokenModel,
)
