package org.p2p.wallet.jupiter.ui.main.mapper

import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.jupiter.model.SwapRateTickerState
import org.p2p.wallet.utils.toPx

class SwapRateTickerMapper {

    fun mapRate(newState: SwapRateTickerState): TextViewCellModel? {
        return when (newState) {
            is SwapRateTickerState.Shown -> mapRateLoaded(newState)
            is SwapRateTickerState.Loading -> mapRateSkeleton(newState)
            is SwapRateTickerState.Hidden -> null
        }
    }

    fun mapRateLoaded(newState: SwapRateTickerState.Shown): TextViewCellModel =
        TextViewCellModel.Raw(TextContainer(newState.formattedNewRate))

    fun mapRateSkeleton(newState: SwapRateTickerState.Loading): TextViewCellModel =
        TextViewCellModel.Skeleton(
            SkeletonCellModel(
                height = 16.toPx(),
                width = 160.toPx(),
                radius = 4f.toPx()
            )
        )
}
