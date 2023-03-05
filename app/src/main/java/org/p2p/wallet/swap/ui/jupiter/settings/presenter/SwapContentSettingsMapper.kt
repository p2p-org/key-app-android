package org.p2p.wallet.swap.ui.jupiter.settings.presenter

import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.formatFiat
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
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.ui.jupiter.main.SwapRateLoaderState
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.emptyString

class SwapContentSettingsMapper(
    private val commonMapper: SwapCommonSettingsMapper,
    private val swapStateManager: SwapStateManager
) {

    suspend fun mapForLoadingTransactionState(
        slippage: Slippage,
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        jupiterTokens: List<JupiterSwapToken>,
        tokenB: SwapTokenModel,
        tokenA: SwapTokenModel,
    ): List<AnyCellItem> = mapList(
        slippage = slippage,
        routes = routes,
        activeRoute = activeRoute,
        jupiterTokens = jupiterTokens,
        tokenBAmount = null,
        tokenB = tokenB,
        tokenA = tokenA,
    )

    suspend fun mapForSwapLoadedState(
        slippage: Slippage,
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        jupiterTokens: List<JupiterSwapToken>,
        tokenBAmount: BigDecimal?,
        tokenB: SwapTokenModel,
        tokenA: SwapTokenModel,
    ): List<AnyCellItem> = mapList(
        slippage = slippage,
        routes = routes,
        activeRoute = activeRoute,
        jupiterTokens = jupiterTokens,
        tokenBAmount = tokenBAmount,
        tokenB = tokenB,
        tokenA = tokenA,
    )

    private suspend fun mapList(
        slippage: Slippage,
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        jupiterTokens: List<JupiterSwapToken>,
        tokenBAmount: BigDecimal?,
        tokenB: SwapTokenModel,
        tokenA: SwapTokenModel,
    ): List<AnyCellItem> = buildList {
        addRouteCell(routes, activeRoute, jupiterTokens)
        this += commonMapper.getNetworkFeeCell()
        addAccountFeeCell(routes, activeRoute, tokenA)
        addLiquidityFeeCell(routes, activeRoute, jupiterTokens)
        addEstimatedFeeCell(routes, activeRoute, tokenA)
        addMinimumReceivedCell(slippage, tokenBAmount, tokenB)
    }

    private fun MutableList<AnyCellItem>.addRouteCell(
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        jupiterTokens: List<JupiterSwapToken>,
    ) {
        val isBestRoute = activeRoute == SwapStateManager.DEFAULT_ACTIVE_ROUTE_ORDINAL
        val activeRoute = routes.getOrNull(activeRoute)
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

    private fun formatRouteString(route: JupiterSwapRoute?, jupiterTokens: List<JupiterSwapToken>): String {
        if (route == null) return emptyString()
        var result = ""
        route.marketInfos.forEachIndexed { index, marketInfo ->
            result = result.plus(jupiterTokens.findTokenSymbolByMint(marketInfo.inputMint)).plus("→")
            if (index == route.marketInfos.lastIndex) {
                result = result.plus(jupiterTokens.findTokenSymbolByMint(marketInfo.outputMint))
            }
        }
        return result
    }

    private fun MutableList<AnyCellItem>.addMinimumReceivedCell(
        slippage: Slippage,
        tokenBAmount: BigDecimal?,
        tokenB: SwapTokenModel
    ) {
        val secondLineText = if (tokenBAmount == null) {
            TextViewCellModel.Skeleton(
                skeleton = leftSubtitleSkeleton()
            )
        } else {
            TextViewCellModel.Raw(
                text = TextContainer(
                    tokenBAmount.minus(tokenBAmount.multiply(slippage.doubleValue.toBigDecimal()))
                        .formatToken(tokenB.decimals).plus(" ${tokenB.tokenSymbol}")
                )
            )
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

    private suspend fun MutableList<AnyCellItem>.addAccountFeeCell(
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        tokenA: SwapTokenModel
    ) {
        val route = routes.getOrNull(activeRoute)
        val ataDeposits = route?.fees?.ataDeposits ?: listOf()
        val openOrdersDeposits = route?.fees?.openOrdersDeposits ?: listOf()
        var accountFee = BigInteger.ZERO
        ataDeposits.forEach {
            accountFee = accountFee.plus(it)
        }
        openOrdersDeposits.forEach {
            accountFee = accountFee.plus(it)
        }
        val fee = accountFee.fromLamports(tokenA.decimals)
        val feeText = fee.formatToken(tokenA.decimals)

        val ratio = swapStateManager.getTokenRate(tokenA)
            .filterIsInstance<SwapRateLoaderState.Loaded>().firstOrNull()
        val feeUsd = ratio?.let { fee.multiply(it.rate).formatFiat() }
            ?.let { usd -> TextViewCellModel.Raw(text = TextContainer(R.string.swap_main_fiat_value, usd)) }

        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_creation_fee_title),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(feeText)
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = feeUsd,
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
        jupiterTokens: List<JupiterSwapToken>
    ) {
        val route = routes.getOrNull(activeRouteIndex)
        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_liquidity_fee_title),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(formatLiquidityFeeString(route, jupiterTokens))
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = null,
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_info_outline),
                    iconTint = R.color.icons_mountain,
                )
            ),
            payload = SwapSettingsPayload.LIQUIDITY_FEE,
            styleType = FinanceBlockStyle.BASE_CELL,
        )
    }

    private fun formatLiquidityFeeString(route: JupiterSwapRoute?, jupiterTokens: List<JupiterSwapToken>): String {
        if (route == null) return emptyString()
        var result = ""
        route.marketInfos.forEachIndexed { index, marketInfo ->
            val lpFee = marketInfo.lpFee
            val lpToken = jupiterTokens.findTokenByMint(lpFee.mint) ?: return@forEachIndexed
            val amount = lpFee.amountInLamports.fromLamports(lpToken.decimals).formatToken(lpToken.decimals)
            val fee = "$amount ${lpToken.tokenSymbol}"
            result = result.plus(fee)
            if (index != route.marketInfos.lastIndex) result = result.plus(", ")
        }
        return result
    }

    private suspend fun MutableList<AnyCellItem>.addEstimatedFeeCell(
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        tokenA: SwapTokenModel
    ) {
        val route = routes.getOrNull(activeRoute)

        val fee = route?.fees?.totalFeeAndDeposits?.fromLamports(tokenA.decimals)

        val feeCell = if (fee != null) {
            val ratio =
                swapStateManager.getTokenRate(tokenA).filterIsInstance<SwapRateLoaderState.Loaded>().firstOrNull()
            ratio?.let { fee.multiply(it.rate).formatFiat() }
                ?.let { usd -> TextViewCellModel.Raw(text = TextContainer(R.string.swap_main_fiat_value, usd)) }
        } else {
            null
        }
        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_estimated_fee_title),
                    textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = feeCell,
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

    private fun List<JupiterSwapToken>.findTokenByMint(mint: Base58String): JupiterSwapToken? {
        return find { it.tokenMint == mint }
    }
}
