package org.p2p.uikit.components

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import android.graphics.drawable.Drawable
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.R
import org.p2p.uikit.model.AnyCellItem

class InformerViewCellModel(
    val leftIcon: LeftIconParams,
    val title: TitleParams? = null,
    val caption: CaptionParams? = null,
    val infoLine: InfoLineParams? = null,
    val onViewClicked: ((item: InformerViewCellModel) -> Unit)? = null,
    val backgroundDrawable: Drawable? = null,
    val backgroundDrawableRes: Int? = null,
    @ColorInt val backgroundTintColor: Int? = null,
    @ColorRes val backgroundTintRes: Int? = null,
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
        @ColorRes val textColorRes: Int? = R.color.text_night,
        val titleIcon: DrawableContainer.Res? = null,
        @ColorRes val titleIconTint: Int = R.color.icons_night
    ) {
        constructor(@StringRes valueRes: Int) : this(TextContainer.invoke(valueRes))
    }

    class CaptionParams(
        val value: TextContainer,
        @ColorRes val textColorRes: Int? = R.color.text_night,
    )

    class InfoLineParams(
        val value: TextContainer,
        val position: InfoLinePosition,
        @ColorRes val textColorRes: Int = R.color.text_mountain,
        val onInfoLineClicked: (() -> Unit)? = null
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
