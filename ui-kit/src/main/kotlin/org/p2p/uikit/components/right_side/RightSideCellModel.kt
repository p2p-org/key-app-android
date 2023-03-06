package org.p2p.uikit.components.right_side

import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel

sealed interface RightSideCellModel {

    data class TwoLineText(
        val firstLineText: TextViewCellModel? = null,
        val secondLineText: TextViewCellModel? = null,
    ) : RightSideCellModel

    data class SingleTextTwoIcon(
        val text: TextViewCellModel? = null,
        val firstIcon: ImageViewCellModel? = null,
        val secondIcon: ImageViewCellModel? = null,
    ) : RightSideCellModel

    data class IconWrapper(
        val iconWrapper: IconWrapperCellModel? = null,
    ) : RightSideCellModel
}
