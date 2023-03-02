package org.p2p.wallet.swap.ui.jupiter.settings.presenter

import java.math.BigDecimal
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.formatToken
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
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.emptyString

class SwapContentSettingsMapper(
    private val commonMapper: SwapCommonSettingsMapper
) {

    fun mapForLoadingTransactionState(
        slippage: Double,
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        jupiterTokens: List<JupiterSwapToken>,
        tokenB: SwapTokenModel,
    ): List<AnyCellItem> = mapList(
        slippage = slippage,
        routes = routes,
        activeRoute = activeRoute,
        jupiterTokens = jupiterTokens,
        tokenBAmount = null,
        tokenB = tokenB
    )

    fun mapForSwapLoadedState(
        slippage: Double,
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        jupiterTokens: List<JupiterSwapToken>,
        tokenBAmount: BigDecimal?,
        tokenB: SwapTokenModel,
    ): List<AnyCellItem> = mapList(
        slippage = slippage,
        routes = routes,
        activeRoute = activeRoute,
        jupiterTokens = jupiterTokens,
        tokenBAmount = tokenBAmount,
        tokenB = tokenB
    )

    private fun mapList(
        slippage: Double,
        routes: List<JupiterSwapRoute>,
        activeRoute: Int,
        jupiterTokens: List<JupiterSwapToken>,
        tokenBAmount: BigDecimal?,
        tokenB: SwapTokenModel,
    ): List<AnyCellItem> = buildList {
        addRouteCell(routes, activeRoute, jupiterTokens)
        this += commonMapper.getNetworkFeeCell()
        addAccountFeeCell()
        addLiquidityFeeCell(routes, activeRoute, jupiterTokens)
        addEstimatedFeeCell()
        addMinimumReceivedCell(slippage, tokenBAmount, tokenB)
        this += commonMapper.createHeader(R.string.swap_settings_slippage_title)
        addAll(commonMapper.getSlippageList(slippage))
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
            result = if (index != route.marketInfos.lastIndex) {
                result.plus(jupiterTokens.findTokenSymbolByMint(marketInfo.inputMint)).plus("→")
            } else {
                result.plus(jupiterTokens.findTokenSymbolByMint(marketInfo.outputMint))
            }
        }
        return result
    }

    private fun MutableList<AnyCellItem>.addMinimumReceivedCell(
        slippage: Double,
        tokenBAmount: BigDecimal?,
        tokenB: SwapTokenModel
    ) {
        val secondLineText = if (tokenBAmount == null) {
            TextViewCellModel.Skeleton(
                skeleton = leftSubtitleSkeleton()
            )
        } else {
            TextViewCellModel.Raw(
                text = TextContainer(tokenBAmount.multiply(slippage.toBigDecimal()).formatToken(tokenB.decimals))
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

    private fun MutableList<AnyCellItem>.addAccountFeeCell() {
        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_creation_fee_title),
                ),
                secondLineText = TextViewCellModel.Skeleton(
                    skeleton = leftSubtitleSkeleton()
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = TextViewCellModel.Skeleton(
                    skeleton = rightSideSkeleton(),
                ),
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
                text = TextViewCellModel.Raw(
                    text = TextContainer("TODO"),
                ),
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
            val fee = "${lpFee.amountInLamports} ${jupiterTokens.findTokenSymbolByMint(lpFee.mint)}"
            result = result.plus(fee)
            if (index != route.marketInfos.lastIndex) result = result.plus(", ")
        }
        return result
    }

    private fun MutableList<AnyCellItem>.addEstimatedFeeCell() {
        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_estimated_fee_title),
                    textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = TextViewCellModel.Raw(
                    text = TextContainer("TODO"),
                ),
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
        return find { it.tokenMint == mint }?.tokenSymbol ?: emptyString()
    }
}
