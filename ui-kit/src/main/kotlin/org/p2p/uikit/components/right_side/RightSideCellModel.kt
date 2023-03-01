package org.p2p.uikit.components.right_side

import androidx.annotation.ColorRes
import org.p2p.core.common.TextContainer
import org.p2p.uikit.R
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel

sealed interface RightSideCellModel {

    data class TextBadge(
        val text: TextContainer,
        @ColorRes val textColor: Int = R.color.text_mountain,
        @ColorRes val badgeTint: Int = R.color.elements_lime,
    ): RightSideCellModel

    data class TwoLineText(
        val firstLineText: TextViewCellModel? = null,
        val secondLineText: TextViewCellModel? = null,
    ) : RightSideCellModel

    data class SingleTextTwoIcon(
        val text: TextViewCellModel? = null,
        val firstIcon: ImageViewCellModel? = null,
        val secondIcon: ImageViewCellModel? = null,
    ) : RightSideCellModel
}
