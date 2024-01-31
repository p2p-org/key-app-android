package org.p2p.wallet.jupiter.ui.settings.presenter

import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
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

class SwapLoadingSettingsMapper(
    private val feeCellsBuilder: SwapFeeCellsBuilder
) {

    suspend fun mapLoadingList(): List<AnyCellItem> = buildList {
        addRouteCell()
        add(feeCellsBuilder.buildNetworkFeeCell(activeRoute = null, solToken = null).cellModel)
        addAccountFeeCell()
        addLiquidityFeeCell()
        addEstimatedFeeCell()
        addMinimumReceivedCell()
    }

    private fun MutableList<AnyCellItem>.addRouteCell() {
        add(
            MainCellModel(
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_settings_route_title),
                    ),
                    secondLineText = TextViewCellModel.Skeleton(
                        skeleton = leftSubtitleSkeleton()
                    ),
                ),
                rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                    text = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_settings_route_best),
                    )
                ),
                payload = SwapSettingsPayload.ROUTE,
                styleType = MainCellStyle.BASE_CELL,
            )
        )
    }

    private fun MutableList<AnyCellItem>.addMinimumReceivedCell() {
        add(
            MainCellModel(
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_settings_minimum_received_title),
                    ),
                    secondLineText = TextViewCellModel.Skeleton(
                        skeleton = leftSubtitleSkeleton()
                    ),
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
        )
    }

    private fun MutableList<AnyCellItem>.addAccountFeeCell() {
        add(
            MainCellModel(
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
                styleType = MainCellStyle.BASE_CELL,
            )
        )
    }

    private fun MutableList<AnyCellItem>.addLiquidityFeeCell() {
        add(
            MainCellModel(
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_settings_liquidity_fee_title),
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
                payload = SwapSettingsPayload.LIQUIDITY_FEE,
                styleType = MainCellStyle.BASE_CELL,
            )
        )
    }

    private fun MutableList<AnyCellItem>.addEstimatedFeeCell() {
        add(
            MainCellModel(
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_settings_estimated_fee_title),
                        textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                    ),
                ),
                rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                    text = TextViewCellModel.Skeleton(
                        skeleton = rightSideSkeleton(),
                    ),
                ),
                payload = SwapSettingsPayload.ESTIMATED_FEE,
                styleType = MainCellStyle.BASE_CELL,
            )
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
