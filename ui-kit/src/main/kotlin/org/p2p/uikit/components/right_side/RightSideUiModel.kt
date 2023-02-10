package org.p2p.uikit.components.right_side

import org.p2p.uikit.utils.image.ImageViewUiModel
import org.p2p.uikit.utils.text.TextViewUiModel

sealed interface RightSideUiModel {

    data class TwoLineText(
        val firstLineText: TextViewUiModel? = null,
        val secondLineText: TextViewUiModel? = null,
    ) : RightSideUiModel

    data class SingleTextTwoIcon(
        val text: TextViewUiModel? = null,
        val firstIcon: ImageViewUiModel? = null,
        val secondIcon: ImageViewUiModel? = null,
    ) : RightSideUiModel
}
