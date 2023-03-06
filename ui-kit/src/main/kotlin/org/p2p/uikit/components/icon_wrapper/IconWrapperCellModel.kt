package org.p2p.uikit.components.icon_wrapper

import org.p2p.uikit.utils.image.ImageViewCellModel

sealed interface IconWrapperCellModel {

    data class SingleIcon(
        val icon: ImageViewCellModel,
    ) : IconWrapperCellModel

    // todo
    /*data class SingleWithBadge(
        val icon: ImageViewUiModel,
        val badge: ImageViewUiModel,
    )*/

    data class TwoIcon(
        val first: ImageViewCellModel,
        val second: ImageViewCellModel,
        val angleType: TwoIconAngle = TwoIconAngle.Plus45,
    ) : IconWrapperCellModel
}

sealed interface TwoIconAngle {

    //horizontal line, first icon on the center of left side, second icon on the center of right side
    object Zero : TwoIconAngle
    //first icon on the left top corner, second icon on the right bottom corner
    object Plus45 : TwoIconAngle
    //horizontal line, first icon on the center of right side, second icon on the center of left side
    object Plus180 : TwoIconAngle
}
