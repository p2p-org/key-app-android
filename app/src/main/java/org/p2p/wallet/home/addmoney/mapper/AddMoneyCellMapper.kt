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
import org.p2p.wallet.home.addmoney.model.AddMoneyItemType

class AddMoneyCellMapper {
    fun getFinanceBlock(
        titleResId: Int,
        subtitleRes: Int,
        iconResId: Int,
        backgroundTintId: Int,
        payload: AddMoneyItemType,
        showRightProgress: Boolean = false,
    ): MainCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            icon = IconWrapperCellModel.SingleIcon(
                icon = ImageViewCellModel(
                    icon = DrawableContainer(iconResId),
                    background = DrawableCellModel(tint = backgroundTintId),
                    clippingShape = shapeCircle()
                )
            ),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(titleResId),
                textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                textColor = R.color.text_night
            ),
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(subtitleRes),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Label1,
                textColor = R.color.text_mountain
            )
        )

        val rightSideCellModel = if (showRightProgress) {
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
            payload = payload
        )
    }
}
