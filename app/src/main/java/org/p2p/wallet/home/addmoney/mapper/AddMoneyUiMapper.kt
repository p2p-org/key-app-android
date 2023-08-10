package org.p2p.wallet.home.addmoney.mapper

import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.ForegroundCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRounded16dp
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.viewState.ViewAccessibilityCellModel
import org.p2p.wallet.R
import org.p2p.wallet.home.addmoney.model.AddMoneyButton
import org.p2p.wallet.home.addmoney.model.AddMoneyButtonType

class AddMoneyUiMapper {
    fun mapToCellItem(item: AddMoneyButton): MainCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            icon = IconWrapperCellModel.SingleIcon(
                icon = ImageViewCellModel(
                    icon = DrawableContainer(item.type.iconResId),
                    background = DrawableCellModel(tint = item.type.backgroundTintId),
                    clippingShape = shapeCircle()
                )
            ),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(item.type.titleResId),
                textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                textColor = R.color.text_night
            ),
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(item.type.subtitleRes),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Label1,
                textColor = R.color.text_mountain
            )
        )

        val rightSideCellModel = if (item.isLoading) {
            RightSideCellModel.Progress(
                indeterminateProgressTint = R.color.night
            )
        } else {
            RightSideCellModel.IconWrapper(
                iconWrapper = IconWrapperCellModel.SingleIcon(
                    icon = ImageViewCellModel(
                        icon = DrawableContainer(R.drawable.ic_chevron_right),
                        iconTint = R.color.icons_mountain
                    )
                )
            )
        }

        return MainCellModel(
            background = DrawableCellModel(
                drawable = shapeDrawable(shapeRounded16dp()),
                tint = R.color.bg_snow
            ),
            foreground = ForegroundCellModel.Ripple(shapeRounded16dp()),
            leftSideCellModel = leftSideCellModel,
            rightSideCellModel = rightSideCellModel,
            accessibility = ViewAccessibilityCellModel(isClickable = true),
            payload = item
        )
    }

    fun mapButtonIsLoading(
        button: AddMoneyButton,
        ifType: AddMoneyButtonType,
        isLoading: Boolean
    ): MainCellModel {
        return if (button.type == ifType) {
            button.copy(isLoading = isLoading)
        } else {
            button
        }.let(::mapToCellItem)
    }
}
