package org.p2p.wallet.jupiter.ui.settings.presenter

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isZero
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.jupiter.ui.main.SwapRateLoaderState
import org.p2p.wallet.jupiter.ui.main.SwapTokenRateLoader

class SwapFeeBuilder(
    private val rateLoader: SwapTokenRateLoader,
    private val swapTokensRepository: JupiterSwapTokensRepository
) {

    suspend fun buildAccountFeeBox(
        activeRoute: JupiterSwapRouteV6,
        solToken: JupiterSwapToken,
    ): SwapSettingFeeBox? {
        if (activeRoute.ataFee.isZero()) return null

        val feeAmountLamports: BigDecimal = activeRoute.ataFee.fromLamports(solToken.decimals)
        val solTokenRate: BigDecimal? = loadRateForToken(solToken)?.rate

        val feeUsd: BigDecimal? = solTokenRate?.let { feeAmountLamports.multiply(it) }

        return SwapSettingFeeBox(
            amountLamports = feeAmountLamports,
            amountUsd = feeUsd,
            token = SwapTokenModel.JupiterToken(solToken)
        )
    }

    suspend fun buildLiquidityFeeListBox(
        activeRoute: JupiterSwapRouteV6,
    ): List<SwapSettingFeeBox>? = supervisorScope {
        val lpTokensRates = activeRoute.routePlans.map { routePlan ->
            val lpToken = swapTokensRepository.findTokenByMint(routePlan.feeMint) ?: return@supervisorScope null
            async { loadRateForToken(lpToken) }
        }
            .awaitAll()
            .filterNotNull()

        activeRoute.routePlans.map { routePlan ->
            val lpToken = lpTokensRates.find { it.token.mintAddress == routePlan.feeMint }
                ?.token
                ?: return@supervisorScope null

            val tokenRate: BigDecimal? = lpTokensRates.find { it.token.mintAddress == routePlan.feeMint }?.rate
            val amountLamports = routePlan.feeAmount.fromLamports(lpToken.decimals)
            val amountUsd = tokenRate?.let { amountLamports.multiply(it) }

            SwapSettingFeeBox(
                amountLamports = amountLamports,
                amountUsd = amountUsd,
                token = lpToken
            )
        }
    }

    private suspend fun loadRateForToken(token: JupiterSwapToken): SwapRateLoaderState.Loaded? {
        return rateLoader.getRate(SwapTokenModel.JupiterToken(token))
            .onEach { Timber.i("JupiterSwapFeeBuilder loading rate for $token") }
            .filterIsInstance<SwapRateLoaderState.Loaded>()
            .firstOrNull()
    }
}

data class SwapSettingFeeBox(
    val amountLamports: BigDecimal,
    val amountUsd: BigDecimal?,
    val token: SwapTokenModel,
)
