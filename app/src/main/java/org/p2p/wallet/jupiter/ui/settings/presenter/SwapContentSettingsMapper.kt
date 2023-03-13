package org.p2p.wallet.jupiter.ui.settings.presenter

import java.math.BigDecimal
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.asUsdSwap
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.finance_block.FinanceBlockStyle
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.model.findTokenByMint
import org.p2p.wallet.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.emptyString

class SwapContentSettingsMapper(
    private val commonMapper: SwapCommonSettingsMapper,
    private val swapFeeLoader: SwapFeeLoader,
) {

    suspend fun mapForLoadingTransactionState(
        slippage: Slippage,
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        jupiterTokens: List<JupiterSwapToken>,
        tokenB: SwapTokenModel,
        tokenA: SwapTokenModel,
        solTokenForFee: JupiterSwapToken?,
    ): List<AnyCellItem> = mapList(
        slippage = slippage,
        routes = routes,
        activeRoute = activeRoute,
        jupiterTokens = jupiterTokens,
        tokenBAmount = null,
        tokenB = tokenB,
        tokenA = tokenA,
        solTokenForFee = solTokenForFee,
    )

    suspend fun mapForSwapLoadedState(
        slippage: Slippage,
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        jupiterTokens: List<JupiterSwapToken>,
        tokenBAmount: BigDecimal?,
        tokenB: SwapTokenModel,
        tokenA: SwapTokenModel,
        solTokenForFee: JupiterSwapToken?,
    ): List<AnyCellItem> = mapList(
        slippage = slippage,
        routes = routes,
        activeRoute = activeRoute,
        jupiterTokens = jupiterTokens,
        tokenBAmount = tokenBAmount,
        tokenB = tokenB,
        tokenA = tokenA,
        solTokenForFee = solTokenForFee,
    )

    private suspend fun mapList(
        slippage: Slippage,
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        jupiterTokens: List<JupiterSwapToken>,
        tokenBAmount: BigDecimal?,
        tokenB: SwapTokenModel,
        tokenA: SwapTokenModel,
        solTokenForFee: JupiterSwapToken?,
    ): List<AnyCellItem> = buildList {
        addRouteCell(routes, activeRoute, jupiterTokens)
        this += commonMapper.getNetworkFeeCell()
        val route = routes.getOrNull(activeRoute)
        val accountFee = swapFeeLoader.getAccountFee(route, solTokenForFee)
        val liquidityFeeList = swapFeeLoader.getLiquidityFeeList(route, jupiterTokens)
        addAccountFeeCell(solTokenForFee, accountFee)
        addLiquidityFeeCell(routes, activeRoute, jupiterTokens, liquidityFeeList)
        addEstimatedFeeCell(accountFee, liquidityFeeList)
        addMinimumReceivedCell(slippage, tokenBAmount, tokenB)
    }

    private fun MutableList<AnyCellItem>.addRouteCell(
        routes: List<JupiterSwapRoute>,
        activeRouteIndex: Int,
        jupiterTokens: List<JupiterSwapToken>,
    ) {
        val isBestRoute = activeRouteIndex == SwapStateManager.DEFAULT_ACTIVE_ROUTE_ORDINAL
        val activeRoute = routes.getOrNull(activeRouteIndex)
        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_route_title),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(formatRouteString(activeRoute, jupiterTokens))
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = if (isBestRoute) {
                    TextViewCellModel.Raw(text = TextContainer(R.string.swap_settings_route_best))
                } else {
                    null
                },
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_chevron_right),
                    iconTint = R.color.icons_mountain,
                )
            ),
            payload = SwapSettingsPayload.ROUTE,
            styleType = FinanceBlockStyle.BASE_CELL,
        )
    }

    private fun formatRouteString(activeRoute: JupiterSwapRoute?, jupiterTokens: List<JupiterSwapToken>): String {
        if (activeRoute == null) return emptyString()
        val marketInfos = activeRoute.marketInfos
        return buildString {
            marketInfos.forEachIndexed { index, marketInfo ->
                append(jupiterTokens.findTokenSymbolByMint(marketInfo.inputMint))
                append(" → ")
                if (index == marketInfos.lastIndex) {
                    append(jupiterTokens.findTokenSymbolByMint(marketInfo.outputMint))
                }
            }
        }
    }

    private fun MutableList<AnyCellItem>.addMinimumReceivedCell(
        slippage: Slippage,
        tokenBAmount: BigDecimal?,
        tokenB: SwapTokenModel
    ) {
        val secondLineText = if (tokenBAmount == null) {
            TextViewCellModel.Skeleton(skeleton = leftSubtitleSkeleton())
        } else {
            val amountWithSlippage = tokenBAmount.multiply(slippage.doubleValue.toBigDecimal())
            val minimumReceivedText = tokenBAmount.minus(amountWithSlippage)
                .formatToken(tokenB.decimals)
                .plus(" ${tokenB.tokenSymbol}")
            TextViewCellModel.Raw(text = TextContainer(minimumReceivedText))
        }
        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_minimum_received_title),
                ),
                secondLineText = secondLineText,
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_info_outline),
                    iconTint = R.color.icons_mountain,
                )
            ),
            payload = SwapSettingsPayload.MINIMUM_RECEIVED,
            styleType = FinanceBlockStyle.BASE_CELL,
        )
    }

    private fun MutableList<AnyCellItem>.addAccountFeeCell(
        solToken: JupiterSwapToken?, // this fee in SOL
        accountFee: SwapSettingFeeBox?,
    ) {
        if (solToken == null || accountFee == null) {
            return
        }

        val feeInTokenText: TextViewCellModel.Raw =
            accountFee.amountLamports.formatToken(solToken.decimals)
                .plus(" ${solToken.tokenSymbol}")
                .let { TextViewCellModel.Raw(text = TextContainer(it)) }

        val feeUsdText: TextViewCellModel.Raw =
            accountFee.amountUsd
                .asUsdSwap()
                .let { usd -> TextViewCellModel.Raw(text = TextContainer(usd)) }

        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_creation_fee_title),
                ),
                secondLineText = feeInTokenText
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = feeUsdText,
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_info_outline),
                    iconTint = R.color.icons_mountain,
                )
            ),
            payload = SwapSettingsPayload.CREATION_FEE,
            styleType = FinanceBlockStyle.BASE_CELL,
        )
    }

    private fun MutableList<AnyCellItem>.addLiquidityFeeCell(
        routes: List<JupiterSwapRoute>,
        activeRouteIndex: Int,
        jupiterTokens: List<JupiterSwapToken>,
        liquidityFeeList: List<SwapSettingFeeBox>?
    ) {
        if (liquidityFeeList == null) return
        val activeRoute = routes.getOrNull(activeRouteIndex)

        val liquidityFeeInTokens = formatLiquidityFeeString(activeRoute, jupiterTokens)
        val liquidityFeeInUsd = liquidityFeeList
            .sumOf { it.amountUsd }
            .asUsdSwap()

        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_liquidity_fee_title),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(liquidityFeeInTokens)
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = liquidityFeeInUsd.let {
                    TextViewCellModel.Raw(text = TextContainer(it))
                },
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_info_outline),
                    iconTint = R.color.icons_mountain,
                )
            ),
            payload = SwapSettingsPayload.LIQUIDITY_FEE,
            styleType = FinanceBlockStyle.BASE_CELL,
        )
    }

    private fun formatLiquidityFeeString(
        route: JupiterSwapRoute?,
        jupiterTokens: List<JupiterSwapToken>
    ): String {
        if (route == null) return emptyString()

        return buildString {
            route.marketInfos.forEachIndexed { index, marketInfo ->
                val lpFee = marketInfo.liquidityFee
                val lpToken = jupiterTokens.findTokenByMint(lpFee.mint) ?: return@forEachIndexed

                val feeAmount = lpFee.amountInLamports
                    .fromLamports(lpToken.decimals)
                    .formatToken(lpToken.decimals)
                val fee = "$feeAmount ${lpToken.tokenSymbol}"
                append(fee)
                if (index != route.marketInfos.lastIndex) {
                    append(", ")
                }
            }
        }
    }

    private fun MutableList<AnyCellItem>.addEstimatedFeeCell(
        accountFee: SwapSettingFeeBox?,
        liquidityFeeList: List<SwapSettingFeeBox>?,
    ) {
        if (accountFee == null || liquidityFeeList == null) {
            return
        }

        val totalFee = liquidityFeeList
            .sumOf { it.amountUsd }
            .plus(accountFee.amountUsd)
            .asUsdSwap()
            .let { usd -> TextViewCellModel.Raw(text = TextContainer(usd)) }

        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_estimated_fee_title),
                    textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = totalFee,
            ),
            payload = SwapSettingsPayload.ESTIMATED_FEE,
            styleType = FinanceBlockStyle.BASE_CELL,
        )
    }

    private fun leftSubtitleSkeleton(): SkeletonCellModel {
        return SkeletonCellModel(
            height = 12.toPx(),
            width = 100.toPx(),
            radius = 4f.toPx(),
        )
    }

    private fun rightSideSkeleton(): SkeletonCellModel {
        return SkeletonCellModel(
            height = 16.toPx(),
            width = 52.toPx(),
            radius = 4f.toPx(),
        )
    }

    private fun List<JupiterSwapToken>.findTokenSymbolByMint(mint: Base58String): String {
        return findTokenByMint(mint)?.tokenSymbol ?: emptyString()
    }
}
