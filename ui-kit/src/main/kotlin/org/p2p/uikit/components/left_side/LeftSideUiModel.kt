package org.p2p.uikit.components.left_side

import org.p2p.uikit.atoms.icon_wrapper.IconWrapperUiModel
import org.p2p.uikit.utils.text.TextViewUiModel

sealed interface LeftSideUiModel {

    data class IconWithText(
        val icon: IconWrapperUiModel? = null,
        val firstLineText: TextViewUiModel? = null,
        val secondLineText: TextViewUiModel? = null,
        val thirdLineText: TextViewUiModel? = null,
    ) : LeftSideUiModel
}
