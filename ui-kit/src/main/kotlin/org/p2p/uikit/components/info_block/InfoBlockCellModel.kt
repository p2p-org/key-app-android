package org.p2p.uikit.components.info_block

import org.p2p.uikit.R
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.model.CellModelPayload
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx

data class InfoBlockCellModel(
    val icon: IconWrapperCellModel? = null,
    val firstLineText: TextViewCellModel? = null,
    val secondLineText: TextViewCellModel? = null,
    val background: DrawableCellModel? = DrawableCellModel(
        drawable = shapeDrawable(shapeRoundedAll(12f.toPx())),
        tint = R.color.bg_cloud
    ),
    override val payload: Any? = null,
) : CellModelPayload, AnyCellItem
