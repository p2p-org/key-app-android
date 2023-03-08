package org.p2p.wallet.swap.ui.jupiter.routes

import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
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
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute

class SwapSelectRoutesMapper {

    fun mapLoadingList(): List<AnyCellItem> = buildList {
        repeat(3) {
            this += FinanceBlockCellModel(
                styleType = FinanceBlockStyle.BASE_CELL,
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = titleSkeleton(),
                    secondLineText = subtitleSkeleton(),
                )
            )
        }
    }

    fun mapRoutesList(routes: List<JupiterSwapRoute>, activeRoute: Int): List<AnyCellItem> = buildList {
        routes.forEachIndexed { index, route ->
            var rightIcon: RightSideCellModel.SingleTextTwoIcon? = null
            if (index == activeRoute) {
                rightIcon = RightSideCellModel.SingleTextTwoIcon(
                    firstIcon = ImageViewCellModel(
                        icon = DrawableContainer(R.drawable.ic_done),
                        iconTint = R.color.icons_night
                    )
                )
            }
            this += FinanceBlockCellModel(
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(formatLabel(route))
                    ),
                    secondLineText = TextViewCellModel.Raw(
                        text = TextContainer("TODO")
                    )
                ),
                rightSideCellModel = rightIcon,
                payload = route,
                styleType = FinanceBlockStyle.BASE_CELL,
            )
        }
    }

    private fun formatLabel(route: JupiterSwapRoute): String = buildString {
        route.marketInfos.forEachIndexed { index, marketInfo ->
            append(marketInfo.label)
            if (index != route.marketInfos.lastIndex) append(" + ")
        }
    }

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
