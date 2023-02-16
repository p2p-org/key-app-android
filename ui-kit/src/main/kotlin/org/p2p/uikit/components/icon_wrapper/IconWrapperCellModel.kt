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
    ) : IconWrapperCellModel
}
