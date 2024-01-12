package org.p2p.wallet.jupiter.ui.routes

import java.math.BigInteger
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
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
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6

class SwapSelectRoutesMapper {

    fun mapLoadingList(): List<AnyCellItem> = buildList {
        repeat(3) {
            this += MainCellModel(
                styleType = MainCellStyle.BASE_CELL,
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = titleSkeleton(),
                    secondLineText = subtitleSkeleton(),
                )
            )
        }
    }

    fun mapRoutesList(
        routes: JupiterSwapRouteV6,
        tokenB: SwapTokenModel
    ): List<AnyCellItem> = buildList {
//        routes.forEachIndexed { index, route ->
//            val rightIcon = if (index == activeRouteIndex) getActiveRouteIcon() else null
//
//            this += MainCellModel(
//                leftSideCellModel = LeftSideCellModel.IconWithText(
//                    firstLineText = TextViewCellModel.Raw(
//                        text = formatRouteName(
//                            route = route
//                        )
//                    ),
//                    secondLineText = TextViewCellModel.Raw(
//                        text = formatPriceDiffText(
//                            currentRoute = route,
//                            currentIndex = index,
//                            bestOutAmount = bestRouteOutAmount,
//                            tokenB = tokenB
//                        )
//                    )
//                ),
//                rightSideCellModel = rightIcon,
//                payload = route,
//                styleType = MainCellStyle.BASE_CELL,
//            )
//        }
    }

    private fun getActiveRouteIcon(): RightSideCellModel.SingleTextTwoIcon {
        return RightSideCellModel.SingleTextTwoIcon(
            firstIcon = ImageViewCellModel(
                icon = DrawableContainer(R.drawable.ic_done),
                iconTint = R.color.icons_night
            )
        )
    }

    private fun formatPriceDiffText(
        currentRoute: JupiterSwapRoute,
        currentIndex: Int,
        bestOutAmount: BigInteger,
        tokenB: SwapTokenModel
    ): TextContainer {
        val isBestPriceRoute = currentIndex == 0
        return if (isBestPriceRoute) {
            TextContainer(R.string.swap_settings_route_best)
        } else {
            val currentRouteOutAmount = currentRoute.outAmountInLamports
            val outAmountDiff = (bestOutAmount - currentRouteOutAmount)
                .fromLamports(tokenB.decimals)
                .formatToken(tokenB.decimals)
            val tokenSymbol = tokenB.tokenSymbol
            TextContainer("-$outAmountDiff $tokenSymbol")
        }
    }

    private fun formatRouteName(route: JupiterSwapRoute): TextContainer = buildString {
        route.marketInfos.forEachIndexed { index, marketInfo ->
            append(marketInfo.label)
            if (index != route.marketInfos.lastIndex) append(" + ")
        }
    }
        .let { TextContainer(it) }

    private fun titleSkeleton(): TextViewCellModel.Skeleton = TextViewCellModel.Skeleton(
        skeleton = SkeletonCellModel(
            height = 16.toPx(),
            width = 212.toPx(),
            radius = 4f.toPx(),
        )
    )

    private fun subtitleSkeleton(): TextViewCellModel.Skeleton = TextViewCellModel.Skeleton(
        skeleton = SkeletonCellModel(
            height = 12.toPx(),
            width = 148.toPx(),
            radius = 4f.toPx(),
        )
    )
}
