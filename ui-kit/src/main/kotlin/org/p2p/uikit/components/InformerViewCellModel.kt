package org.p2p.uikit.components

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.R
import org.p2p.uikit.model.AnyCellItem

class InformerViewCellModel(
    val leftIcon: LeftIconParams,
    val title: TitleParams? = null,
    val caption: TextContainer? = null,
    val infoLine: InfoLineParams? = null,
    val onViewClicked: ((item: InformerViewCellModel) -> Unit)? = null
) : AnyCellItem {

    class LeftIconParams(
        val icon: DrawableContainer,
        @ColorRes val iconTint: Int = R.color.icons_mountain,
        @ColorRes val backgroundTint: Int = R.color.bg_mountain
    ) {
        constructor(iconRes: Int) : this(DrawableContainer.invoke(iconRes))
    }

    class TitleParams(
        val value: TextContainer,
        val titleIcon: DrawableContainer.Res? = null,
        @ColorRes val titleIconTint: Int = R.color.icons_night
    ) {
        constructor(@StringRes valueRes: Int) : this(TextContainer.invoke(valueRes))
    }

    class InfoLineParams(
        val value: TextContainer,
        val position: InfoLinePosition,
        @ColorRes val textColorRes: Int = R.color.text_mountain,
        val onInfoLineClicked: ((value: CharSequence) -> Unit)? = null
    ) {
        enum class InfoLinePosition {
            BOTTOM, CAPTION_LINE
        }

        constructor(
            @StringRes valueRes: Int,
            position: InfoLinePosition
        ) : this(TextContainer.invoke(valueRes), position)
    }
}
