package org.p2p.wallet.jupiter.ui.settings.presenter

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.model.findTokenByMint
import org.p2p.wallet.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.jupiter.ui.main.SwapRateLoaderState

class JupiterSwapFeeBuilder(
    private val swapStateManager: SwapStateManager,
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

        val solTokenRate: BigDecimal? = loadRateForToken(solToken)?.rate

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
            val lpToken = jupiterTokens.findTokenByMint(lpFee.mint) ?: return@withContext null
            async { loadRateForToken(lpToken) }
        }
            .awaitAll()
            .filterNotNull()

        activeRoute.marketInfos.map { marketInfo ->
            val lpFee = marketInfo.liquidityFee
            val lpToken = jupiterTokens.findTokenByMint(lpFee.mint) ?: return@withContext null
            val tokenRate: BigDecimal? = lpTokensRates.find { it.token.mintAddress == lpFee.mint }?.rate
            val amountLamports = lpFee.amountInLamports.fromLamports(lpToken.decimals)
            val amountUsd = tokenRate?.let { amountLamports.multiply(it) }

            SwapSettingFeeBox(
                amountLamports = amountLamports,
                amountUsd = amountUsd,
                token = lpToken
            )
        }
    }

    private suspend fun loadRateForToken(token: JupiterSwapToken): SwapRateLoaderState.Loaded? {
        return swapStateManager.getTokenRate(SwapTokenModel.JupiterToken(token))
            .onEach { Timber.i("JupiterSwapFeeBuilder loading rate for $token") }
            .filterIsInstance<SwapRateLoaderState.Loaded>()
            .firstOrNull()
    }
}

data class SwapSettingFeeBox(
    val amountLamports: BigDecimal,
    val amountUsd: BigDecimal?,
    val token: JupiterSwapToken,
)

