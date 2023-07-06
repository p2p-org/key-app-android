package org.p2p.uikit.components.right_side

import androidx.annotation.ColorRes
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel

// todo (PWN-9102): add Button state
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

    data class Progress(
        @ColorRes val indeterminateProgressTint: Int? = null
    ) : RightSideCellModel
}
