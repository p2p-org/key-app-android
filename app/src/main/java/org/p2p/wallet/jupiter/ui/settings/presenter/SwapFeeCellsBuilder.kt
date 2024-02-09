package org.p2p.wallet.jupiter.ui.settings.presenter

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.asUsdSwap
import org.p2p.core.utils.divideSafe
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.formatTokenWithSymbol
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellStyle
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.jupiter.ui.main.SwapRateLoaderState
import org.p2p.wallet.jupiter.ui.main.SwapTokenRateLoader

class SwapSettingsFeeBox(
    val cellModel: MainCellModel,
    val feeInUsd: BigDecimal?
)

class SwapFeeCellsBuilder(
    private val rateLoader: SwapTokenRateLoader,
    private val swapTokensRepository: JupiterSwapTokensRepository
) {

    suspend fun buildNetworkFeeCell(
        activeRoute: JupiterSwapRouteV6?,
        solToken: JupiterSwapToken,
    ): SwapSettingsFeeBox {
        if (activeRoute != null) {
            val networkFee = activeRoute.fees.signatureFee.fromLamports(solToken.decimals)
            val solTokenRate: BigDecimal? = loadRateForToken(SwapTokenModel.JupiterToken(solToken))?.rate
            val feeUsd: BigDecimal? = solTokenRate?.let { networkFee.multiply(it) }
            // amount is not used, network fee is free right now
        }
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

        val solRate = loadRateForToken(SwapTokenModel.JupiterToken(solToken))?.rate ?: kotlin.run {
            Timber.e(IllegalStateException("Sol rate is null"))
            return null
        }
        val tokenBRate = loadRateForToken(tokenB)?.rate ?: kotlin.run {
            Timber.e(IllegalStateException("Token B (${tokenB.mintAddress} rate is null"))
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
    ): SwapSettingsFeeBox? = supervisorScope {
        val lpTokensRates = activeRoute.routePlans.map { routePlan ->
            val lpToken = swapTokensRepository.findTokenByMint(routePlan.feeMint) ?: return@supervisorScope null
            async { loadRateForToken(SwapTokenModel.JupiterToken(lpToken)) }
        }
            .awaitAll()
            .filterNotNull()

        val feeAmountFormatted = formatLiquidityFeeString(activeRoute)

        val usdAmounts = activeRoute.routePlans.map { routePlan ->
            val (lpToken, tokenRate) = lpTokensRates
                .find { it.token.mintAddress == routePlan.feeMint }
                ?: return@map null

            val amountLamports = routePlan.feeAmount.fromLamports(lpToken.decimals)
            amountLamports * tokenRate
        }

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

    private suspend fun formatLiquidityFeeString(route: JupiterSwapRouteV6): String {
        return buildString {
            route.routePlans.forEachIndexed { index, routePlan ->
                val lpFee = routePlan.feeAmount
                val lpToken = swapTokensRepository.findTokenByMint(routePlan.feeMint) ?: return@forEachIndexed

                val feeAmount = lpFee
                    .fromLamports(lpToken.decimals)
                    .formatToken(lpToken.decimals)
                val fee = "$feeAmount ${lpToken.tokenSymbol}"
                append(fee)
                if (index != route.routePlans.lastIndex) {
                    append(", ")
                }
            }
        }
    }

    suspend fun buildKeyAppFee(activeRoute: JupiterSwapRouteV6, tokenB: SwapTokenModel): SwapSettingsFeeBox? {
        val platformFee = activeRoute.fees.platformFeeTokenB
        if (platformFee.isZero()) {
            return null
        }

        val tokenBRate = loadRateForToken(tokenB)?.rate

        val platformFeeUsd = activeRoute.fees.platformFeeTokenB
            .fromLamports(tokenB.decimals)
            .multiply(tokenBRate.orZero())

        val formattedPlatformFee = "${activeRoute.fees.platformFeePercent.formatFiat()} %"

        val cellModel = MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer("KeyApp Fee"),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(formattedPlatformFee)
                ),
            ),
            rightSideCellModel = null,
            styleType = MainCellStyle.BASE_CELL,
        )
        return SwapSettingsFeeBox(
            cellModel = cellModel,
            feeInUsd = platformFeeUsd
        )
    }

    fun buildEstimatedFeeString(
        networkFees: SwapSettingsFeeBox,
        accountFee: SwapSettingsFeeBox?,
        liquidityFees: SwapSettingsFeeBox?,
        token2022Fee: SwapSettingsFeeBox?,
        keyAppFee: SwapSettingsFeeBox?,
    ): MainCellModel? {
        if (liquidityFees?.feeInUsd == null) {
            return null
        }

        val totalFee: String = liquidityFees.feeInUsd
            .plus(accountFee?.feeInUsd.orZero())
            .plus(token2022Fee?.feeInUsd.orZero())
            .plus(keyAppFee?.feeInUsd.orZero())
            .asUsdSwap()

        return MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_estimated_fee_title),
                    textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = totalFee.let {
                    TextViewCellModel.Raw(text = TextContainer(it))
                },
            ),
            payload = SwapSettingsPayload.ESTIMATED_FEE,
            styleType = MainCellStyle.BASE_CELL,
        )
    }

    private suspend fun loadRateForToken(token: SwapTokenModel): SwapRateLoaderState.Loaded? {
        return rateLoader.getRate(token)
            .onEach { Timber.i("JupiterSwapFeeBuilder getting rate for ${token.tokenSymbol}") }
            .filterIsInstance<SwapRateLoaderState.Loaded>()
            .firstOrNull()
    }
}
