package org.p2p.wallet.settings.ui.recovery

import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewBackgroundModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.badgeRounded
import org.p2p.wallet.R
import org.p2p.wallet.utils.toPx

class SecurityCellMapper {

    fun mapDeviceCell(deviceName: String): MainCellModel {
        val icon = IconWrapperCellModel.SingleIcon(
            ImageViewCellModel(DrawableContainer(R.drawable.ic_recovery_device))
        )
        val firstLineText = TextViewCellModel.Raw(
            text = TextContainer(R.string.recovery_device_title),
            textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
            textColor = R.color.text_night
        )

        val leftSideCellModel = LeftSideCellModel.IconWithText(
            icon = icon,
            firstLineText = firstLineText,
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(deviceName),
                textColor = R.color.text_night
            )
        )
        val rightSideCellModel = RightSideCellModel.TwoLineText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.devices_subtitle),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text4,
                textColor = R.color.text_night,
                badgeBackground = TextViewBackgroundModel(
                    background = badgeRounded(
                        cornerSize = 8f.toPx(),
                        tint = R.color.button_rain
                    )
                )
            )
        )
        return MainCellModel(
            leftSideCellModel = leftSideCellModel,
            rightSideCellModel = rightSideCellModel
        )
    }

    fun mapPhoneCell(phone: String): MainCellModel {
        val icon = IconWrapperCellModel.SingleIcon(
            ImageViewCellModel(DrawableContainer(R.drawable.ic_recovery_phone))
        )
        val firstLineText = TextViewCellModel.Raw(
            text = TextContainer(R.string.recovery_phone_title),
            textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
            textColor = R.color.text_night
        )

        val leftSideCellModel = LeftSideCellModel.IconWithText(
            icon = icon,
            firstLineText = firstLineText,
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(phone),
                textColor = R.color.text_night
            )
        )
        val rightSideCellModel = RightSideCellModel.TwoLineText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.recovery_manage),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text4,
                textColor = R.color.text_night,
                badgeBackground = TextViewBackgroundModel(
                    background = badgeRounded(
                        cornerSize = 8f.toPx(),
                        tint = R.color.button_rain
                    )
                )
            )
        )
        return MainCellModel(
            leftSideCellModel = leftSideCellModel,
            rightSideCellModel = rightSideCellModel
        )
    }

    fun mapEmailCell(email: String): MainCellModel {
        val icon = IconWrapperCellModel.SingleIcon(
            ImageViewCellModel(DrawableContainer(R.drawable.ic_recovery_social))
        )
        val firstLineText = TextViewCellModel.Raw(
            text = TextContainer(R.string.recovery_social_title),
            textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
            textColor = R.color.text_night
        )

        val leftSideCellModel = LeftSideCellModel.IconWithText(
            icon = icon,
            firstLineText = firstLineText,
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(email),
                textColor = R.color.text_night
            )
        )
        val rightSideCellModel = RightSideCellModel.TwoLineText(
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.recovery_manage),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Text4,
                textColor = R.color.text_night,
                badgeBackground = TextViewBackgroundModel(
                    background = badgeRounded(
                        cornerSize = 8f.toPx(),
                        tint = R.color.button_rain
                    )
                )
            )
        )
        return MainCellModel(
            leftSideCellModel = leftSideCellModel,
            rightSideCellModel = rightSideCellModel
        )
    }
}
