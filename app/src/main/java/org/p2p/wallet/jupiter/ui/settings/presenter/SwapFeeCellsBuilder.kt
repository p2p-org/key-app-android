package org.p2p.wallet.jupiter.ui.settings.presenter

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.supervisorScope
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.crypto.Base58String
import org.p2p.core.utils.asUsdSwap
import org.p2p.core.utils.divideSafe
import org.p2p.core.utils.formatTokenWithSymbol
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellStyle
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoutePlanV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository

class SwapSettingsFeeBox(
    val cellModel: MainCellModel,
    val feeInUsd: BigDecimal?
)

class SwapFeeCellsBuilder(
    private val tokenServiceRepository: TokenServiceRepository,
    private val swapTokensRepository: JupiterSwapTokensRepository
) {

    private class SwapFeeBuildFailed(
        message: String,
        cause: Throwable? = null
    ) : Exception(message, cause)

    suspend fun buildNetworkFeeCell(
        activeRoute: JupiterSwapRouteV6?,
        solToken: JupiterSwapToken,
    ): SwapSettingsFeeBox {
        // amount is not used, network fee is free right now
//        val networkFee = activeRoute.fees.signatureFee.fromLamports(solToken.decimals)
//        val solTokenRate: BigDecimal? = loadRateForToken(SwapTokenModel.JupiterToken(solToken))?.rate
//        val feeUsd: BigDecimal? = solTokenRate?.let { networkFee.multiply(it) }

        val cellModel = MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_network_fee_title),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_network_fee_subtitle),
                    textColor = R.color.text_mint,
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_network_fee_free),
                    textColor = R.color.text_mint,
                ),
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_info_outline),
                    iconTint = R.color.icons_mint,
                )
            ),
            payload = SwapSettingsPayload.NETWORK_FEE,
            styleType = MainCellStyle.BASE_CELL,
        )

        return SwapSettingsFeeBox(
            cellModel = cellModel,
            feeInUsd = BigDecimal.ZERO
        )
    }

    suspend fun buildAccountFeeCell(
        activeRoute: JupiterSwapRouteV6,
        tokenB: SwapTokenModel,
        solToken: JupiterSwapToken
    ): SwapSettingsFeeBox? {
        val ataFeeInSol = activeRoute.fees.ataDepositsInSol.fromLamports(solToken.decimals)
        if (ataFeeInSol.isZero()) {
            return null
        }

        val solRate = loadRateForToken(solToken.tokenMint)?.usdRate ?: kotlin.run {
            Timber.e(SwapFeeBuildFailed("Sol rate is null"))
            return null
        }
        val tokenBRate = loadRateForToken(tokenB.mintAddress)?.usdRate ?: kotlin.run {
            Timber.e(SwapFeeBuildFailed("Token B (${tokenB.mintAddress} rate is null"))
            return null
        }
        val ataFeeInTokenB: BigDecimal = solRate.divideSafe(tokenBRate) * ataFeeInSol

        val feeUsd = ataFeeInTokenB.multiply(tokenBRate)

        val formattedFeeAmount = ataFeeInTokenB.formatTokenWithSymbol(tokenB.tokenSymbol, tokenB.decimals)

        val cellModel = MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_creation_fee_title),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(formattedFeeAmount),
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = feeUsd?.let {
                    TextViewCellModel.Raw(TextContainer(it.asUsdSwap()))
                },
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_info_outline),
                    iconTint = R.color.icons_mountain,
                )
            ),
            payload = SwapSettingsPayload.CREATION_FEE,
            styleType = MainCellStyle.BASE_CELL,
        )

        return SwapSettingsFeeBox(
            cellModel = cellModel,
            feeInUsd = feeUsd
        )
    }

    suspend fun buildLiquidityFeeCell(
        activeRoute: JupiterSwapRouteV6,
        tokenB: SwapTokenModel,
    ): SwapSettingsFeeBox = supervisorScope {
        val keyAppFee = activeRoute.fees.platformFeeTokenB

        val feeAmountFormatted = formatLiquidityFeeString(
            routePlans = activeRoute.routePlans,
            keyAppFee = keyAppFee,
            tokenB = tokenB
        )
        val usdAmounts = getLiquidityFeeUsdRates(
            routePlans = activeRoute.routePlans,
            keyAppFee = keyAppFee,
            tokenB = tokenB
        )

        val someAmountsUsdNotLoaded = usdAmounts.any { it == null }
        val liquidityFeeInUsd = if (someAmountsUsdNotLoaded) {
            null
        } else {
            usdAmounts.sumOf(BigDecimal?::orZero)
        }

        val cellModel = MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_liquidity_fee_title),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(feeAmountFormatted)
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = liquidityFeeInUsd?.let {
                    TextViewCellModel.Raw(text = TextContainer(it.asUsdSwap()))
                },
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_info_outline),
                    iconTint = R.color.icons_mountain,
                )
            ),
            payload = SwapSettingsPayload.LIQUIDITY_FEE,
            styleType = MainCellStyle.BASE_CELL,
        )
        SwapSettingsFeeBox(cellModel, liquidityFeeInUsd)
    }

    /**
     * Append KeyApp fee to liquidity fee string
     */
    private suspend fun formatLiquidityFeeString(
        routePlans: List<JupiterSwapRoutePlanV6>,
        keyAppFee: BigInteger,
        tokenB: SwapTokenModel
    ): String {
        return buildString {
            routePlans.forEachIndexed { index, routePlan ->
                val lpToken = swapTokensRepository.findTokenByMint(routePlan.feeMint) ?: return@forEachIndexed

                val lpFee = routePlan.feeAmount
                val feeAmount = lpFee
                    .fromLamports(lpToken.decimals)
                    .formatTokenWithSymbol(lpToken.tokenSymbol, lpToken.decimals)
                append(feeAmount)
                if (index != routePlans.lastIndex) {
                    append(", ")
                }
            }
            if (keyAppFee.isNotZero()) {
                append(", ")
                val feeAmount = keyAppFee
                    .fromLamports(tokenB.decimals)
                    .formatTokenWithSymbol(tokenB.tokenSymbol, tokenB.decimals)
                append(feeAmount)
            }
        }
    }

    private suspend fun getLiquidityFeeUsdRates(
        routePlans: List<JupiterSwapRoutePlanV6>,
        keyAppFee: BigInteger,
        tokenB: SwapTokenModel,
    ): List<BigDecimal?> {
        val tokenBRate = loadRateForToken(tokenB.mintAddress)?.usdRate
        val keyAppFeeUsd = tokenBRate?.let { it * keyAppFee.fromLamports(tokenB.decimals) }

        return routePlans.map { routePlan ->
            val lpToken = swapTokensRepository.findTokenByMint(routePlan.feeMint) ?: return@map null
            val usdRate = loadRateForToken(lpToken.tokenMint)?.usdRate ?: return@map null
            val amountLamports = routePlan.feeAmount.fromLamports(lpToken.decimals)

            amountLamports * usdRate
        }
            .plus(keyAppFeeUsd)
    }

    fun buildEstimatedFeeString(
        networkFees: SwapSettingsFeeBox,
        accountFee: SwapSettingsFeeBox?,
        liquidityFees: SwapSettingsFeeBox,
        token2022Fee: SwapSettingsFeeBox?,
    ): MainCellModel? {
        val totalFee = liquidityFees.feeInUsd.orZero()
            .plus(accountFee?.feeInUsd.orZero())
            .plus(token2022Fee?.feeInUsd.orZero())

        if (totalFee.isZero()) return null

        return MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_estimated_fee_title),
                    textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = TextViewCellModel.Raw(text = TextContainer(totalFee.asUsdSwap()))
            ),
            payload = SwapSettingsPayload.ESTIMATED_FEE,
            styleType = MainCellStyle.BASE_CELL,
        )
    }

    private suspend fun loadRateForToken(tokenMint: Base58String): TokenServicePrice? {
        return kotlin.runCatching {
            tokenServiceRepository.getTokenPriceByAddress(
                tokenAddress = tokenMint.base58Value,
                networkChain = TokenServiceNetwork.SOLANA
            )
        }
            .onFailure { Timber.e(SwapFeeBuildFailed("Failed to get rate for $tokenMint", it)) }
            .getOrNull()
    }
}
