package org.p2p.uikit.components.icon_wrapper

import com.google.android.material.shape.ShapeAppearanceModel
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel

sealed interface IconWrapperCellModel {

    /**
     * @param sizePx fixed icon size in px
     */
    data class SingleIcon(
        val icon: ImageViewCellModel,
        val sizePx: Int? = null
    ) : IconWrapperCellModel

    data class SingleEmoji(
        val emoji: String,
        val background: DrawableCellModel? = null,
        val foreground: DrawableCellModel? = null,
        val clippingShape: ShapeAppearanceModel? = null,
    ) : IconWrapperCellModel

    // todo
    /*data class SingleWithBadge(
        val icon: ImageViewUiModel,
        val badge: ImageViewUiModel,
    )*/

    data class TwoIcon(
        val first: ImageViewCellModel?,
        val second: ImageViewCellModel?,
        val angleType: TwoIconAngle = TwoIconAngle.Plus45,
    ) : IconWrapperCellModel
}

sealed interface TwoIconAngle {
    // horizontal line, first icon on the center of left side, second icon on the center of right side
    object Zero : TwoIconAngle

    // first icon on the left top corner, second icon on the right bottom corner
    object Plus45 : TwoIconAngle

    // horizontal line, first icon on the center of right side, second icon on the center of left side
    object Plus180 : TwoIconAngle
}
