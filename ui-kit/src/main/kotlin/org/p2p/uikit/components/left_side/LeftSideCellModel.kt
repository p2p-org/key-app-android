package org.p2p.uikit.components.left_side

import android.text.TextUtils
import org.p2p.core.common.TextContainer
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
    ) : LeftSideCellModel {
        /**
         * Easier way to create model if you don't need to override TextAppearance inside the view
         * we have predefined styles already in xml
         */
        constructor(
            firstLineText: TextContainer? = null,
            secondLineText: TextContainer? = null,
            thirdLineText: TextContainer? = null
        ) : this(
            icon = null,
            firstLineText?.let(TextViewCellModel::Raw),
            secondLineText?.let(TextViewCellModel::Raw),
            thirdLineText?.let(TextViewCellModel::Raw),
        )
    }
}
