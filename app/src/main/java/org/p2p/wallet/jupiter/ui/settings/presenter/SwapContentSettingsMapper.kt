package org.p2p.wallet.jupiter.ui.settings.presenter

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.utils.formatTokenWithSymbol
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellStyle
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.utils.emptyString

class SwapContentSettingsMapper(
    private val swapFeeBuilder: SwapFeeCellsBuilder,
    private val token2022FeeBuilder: SwapToken2022FeeBuilder,
    private val dispatchers: CoroutineDispatchers,
    private val swapTokensRepository: JupiterSwapTokensRepository
) {
    suspend fun mapForLoadingTransactionState(
        state: SwapState.LoadingTransaction,
        solTokenForFee: JupiterSwapToken,
    ): List<AnyCellItem> = mapList(
        slippage = state.slippage,
        route = state.route,
        tokenA = state.tokenA,
        tokenAAmount = state.amountTokenA,
        tokenBAmount = null,
        tokenB = state.tokenB,
        solTokenForFee = solTokenForFee,
    )

    suspend fun mapForLoadingTransactionState(
        state: SwapState.RoutesLoaded,
        solTokenForFee: JupiterSwapToken,
    ): List<AnyCellItem> = mapList(
        slippage = state.slippage,
        route = state.route,
        tokenA = state.tokenA,
        tokenAAmount = state.amountTokenA,
        tokenBAmount = null,
        tokenB = state.tokenB,
        solTokenForFee = solTokenForFee,
    )

    suspend fun mapForRoutesLoadedState(
        state: SwapState.RoutesLoaded,
        solTokenForFee: JupiterSwapToken,
    ): List<AnyCellItem> = mapList(
        slippage = state.slippage,
        route = state.route,
        tokenAAmount = state.amountTokenA,
        tokenBAmount = state.amountTokenB,
        tokenA = state.tokenA,
        tokenB = state.tokenB,
        solTokenForFee = solTokenForFee,
        showMinimumReceivedAmount = true,
    )

    suspend fun mapForSwapLoadedState(
        state: SwapState.SwapLoaded,
        solTokenForFee: JupiterSwapToken,
    ): List<AnyCellItem> = mapList(
        slippage = state.slippage,
        route = state.route,
        tokenAAmount = state.amountTokenA,
        tokenBAmount = state.amountTokenB,
        tokenA = state.tokenA,
        tokenB = state.tokenB,
        solTokenForFee = solTokenForFee,
    )

    /**
     * Complex mapping logic, we need io thread here
     */
    private suspend fun mapList(
        slippage: Slippage,
        route: JupiterSwapRouteV6,
        tokenAAmount: BigDecimal,
        tokenBAmount: BigDecimal?,
        tokenA: SwapTokenModel,
        tokenB: SwapTokenModel,
        solTokenForFee: JupiterSwapToken,
        showMinimumReceivedAmount: Boolean = true,
    ): List<AnyCellItem> = withContext(dispatchers.io) {
        buildList {
            addRouteCell(route)

            val networkFeeCell = swapFeeBuilder.buildNetworkFeeCell(route, solTokenForFee)
            this += networkFeeCell.cellModel

            val accountFee = swapFeeBuilder.buildAccountFeeCell(route, tokenB, solTokenForFee)
            if (accountFee != null) {
                this += accountFee.cellModel
            }

            val token2022Fee = token2022FeeBuilder.buildToken2022Fees(
                tokenA = tokenA,
                tokenAAmount = tokenAAmount,
                tokenB = tokenB,
                tokenBAmount = tokenBAmount
            )
            if (token2022Fee?.cellModel != null) {
                this += token2022Fee.cellModel
            }

            val liquidityFee = swapFeeBuilder.buildLiquidityFeeCell(route, tokenB)
            this += liquidityFee.cellModel

            val estimatedFee = swapFeeBuilder.buildEstimatedFeeString(
                networkFees = networkFeeCell,
                accountFee = accountFee,
                liquidityFees = liquidityFee,
                token2022Fee = token2022Fee,
            )
            if (estimatedFee != null) {
                this += estimatedFee
            }

            Timber.i("SwapContentSettingsMapper: accountFee=$accountFee; liquidityFeeList=$liquidityFee")

            addMinimumReceivedCell(
                slippage = slippage,
                tokenBAmount = tokenBAmount,
                tokenB = tokenB,
                showMinimumReceivedAmount = showMinimumReceivedAmount
            )
        }
    }

    private suspend fun MutableList<AnyCellItem>.addRouteCell(
        route: JupiterSwapRouteV6,
    ) {
        val routeAsString = formatRouteString(route)

        this += MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_route_title),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(routeAsString)
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                text = TextViewCellModel.Raw(text = TextContainer(R.string.swap_settings_route_best)),
            ),
            payload = SwapSettingsPayload.ROUTE,
            styleType = MainCellStyle.BASE_CELL,
        )
    }

    private suspend fun formatRouteString(activeRoute: JupiterSwapRouteV6?): String {
        if (activeRoute == null) return emptyString()
        return buildString {
            activeRoute.routePlans.forEachIndexed { index, routePlan ->
                val inputSymbol = swapTokensRepository.findTokenByMint(routePlan.inputMint)?.tokenSymbol ?: "??"
                append(inputSymbol)
                append(" → ")
                if (index == activeRoute.routePlans.lastIndex) {
                    val outputSymbol = swapTokensRepository.findTokenByMint(routePlan.outputMint)?.tokenSymbol ?: "??"
                    append(outputSymbol)
                }
            }
        }
    }

    private fun MutableList<AnyCellItem>.addMinimumReceivedCell(
        slippage: Slippage,
        tokenBAmount: BigDecimal?,
        tokenB: SwapTokenModel,
        showMinimumReceivedAmount: Boolean
    ) {
        if (!showMinimumReceivedAmount) return
        val secondLineText = if (tokenBAmount == null) {
            TextViewCellModel.Skeleton(skeleton = leftSubtitleSkeleton())
        } else {
            val amountWithSlippage = tokenBAmount.multiply(slippage.doubleValue.toBigDecimal())
            val minimumReceivedText = tokenBAmount.minus(amountWithSlippage)
                .formatTokenWithSymbol(tokenB.tokenSymbol, tokenB.decimals)
            TextViewCellModel.Raw(text = TextContainer(minimumReceivedText))
        }
        this += MainCellModel(
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
            styleType = MainCellStyle.BASE_CELL,
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
}
