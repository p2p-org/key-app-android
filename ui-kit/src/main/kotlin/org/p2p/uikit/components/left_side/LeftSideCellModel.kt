package org.p2p.uikit.components.left_side

import android.text.TextUtils
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.utils.text.TextViewCellModel

sealed interface LeftSideCellModel {

    data class IconWithText(
        val icon: IconWrapperCellModel? = null,
        val firstLineText: TextViewCellModel? = null,
        val secondLineText: TextViewCellModel? = null,
        val thirdLineText: TextViewCellModel? = null,
        // styling
        val firstLineTextEllipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.MIDDLE,
    ) : LeftSideCellModel
}
