package org.p2p.uikit.components.icon_wrapper

import org.p2p.uikit.utils.image.ImageViewUiModel

sealed interface IconWrapperUiModel {

    data class SingleIcon(
        val icon: ImageViewUiModel,
    ) : IconWrapperUiModel

    // todo
    /*data class SingleWithBadge(
        val icon: ImageViewUiModel,
        val badge: ImageViewUiModel,
    )*/

    data class TwoIcon(
        val first: ImageViewUiModel,
        val second: ImageViewUiModel,
    ) : IconWrapperUiModel
}
