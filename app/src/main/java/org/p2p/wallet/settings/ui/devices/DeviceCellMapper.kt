package org.p2p.wallet.settings.ui.devices

import androidx.annotation.StringRes
import kotlinx.coroutines.withContext
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.dip
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewBackgroundModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.TextViewSize
import org.p2p.uikit.utils.text.badgePadding
import org.p2p.uikit.utils.text.badgeRounded
import org.p2p.wallet.R
import org.p2p.wallet.utils.toPx

class DeviceCellMapper(
    private val dispatchers: CoroutineDispatchers
) {

    private val mainCellHorizontalMargin = 12.toPx()

    suspend fun toCellModels(
        currentDeviceName: String,
        oldDeviceName: String
    ): List<AnyCellItem> = withContext(dispatchers.io) {

        val cells = mutableListOf<AnyCellItem>()

        cells += createHeaderCell()

        cells += createSectionCell(R.string.devices_this_device)

        cells += createDeviceCell(currentDeviceName, buttonRes = R.string.devices_set_up)

        cells += createSectionCell(R.string.devices_authorization_device)

        cells += createDeviceCell(oldDeviceName, R.string.devices_make_sure_your_device)

        cells += createFooterCell()

        return@withContext cells
    }

    private fun createHeaderCell(): TextViewCellModel {
        return TextViewCellModel.Raw(
            text = TextContainer(R.string.devices_subtitle),
            textAppearance = R.style.UiKit_TextAppearance_Regular_Text3,
            textColor = R.color.text_night,
            badgeBackground = TextViewBackgroundModel(
                background = DrawableCellModel(),
                padding = badgePadding(
                    left = dip(16),
                    right = dip(16),
                    top = dip(24),
                    bottom = dip(24)
                )
            )
        )
    }

    private fun createSectionCell(@StringRes title: Int): SectionHeaderCellModel {
        return SectionHeaderCellModel(
            sectionTitle = TextContainer(title),
            textColor = R.color.text_mountain,
            isShevronVisible = false
        )
    }

    private fun createDeviceCell(
        title: String,
        @StringRes subtitle: Int? = null,
        @StringRes buttonRes: Int? = null
    ): MainCellModel {
        val icon = IconWrapperCellModel.SingleIcon(
            ImageViewCellModel(DrawableContainer(R.drawable.ic_recovery_device))
        )
        val secondLineText = subtitle?.let {
            TextViewCellModel.Raw(
                text = TextContainer(subtitle),
                textColor = R.color.text_rose,
                textAppearance = R.style.UiKit_TextAppearance_Regular_Label1,
                textSize = TextViewSize(textSize = 12f)
            )
        }
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            icon = icon,
            firstLineText = TextViewCellModel.Raw(TextContainer(title)),
            secondLineText = secondLineText
        )
        val rightSideCellModel = buttonRes?.let {
            RightSideCellModel.TwoLineText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(it),
                    textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text4,
                    textSize = TextViewSize(textSize = 13f),
                    textColor = R.color.text_night,
                    badgeBackground = TextViewBackgroundModel(
                        background = badgeRounded(cornerSize = 8f.toPx(), tint = R.color.button_rain),
                        padding = badgePadding(left = 12.toPx(), right = 12.toPx(), top = 8.toPx(), bottom = 8.toPx())
                    )
                )
            )
        }

        val roundedBackground = DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(cornerSize = 16f.toPx())),
            tint = R.color.bg_snow
        )

        return MainCellModel(
            leftSideCellModel = leftSideCellModel,
            rightSideCellModel = rightSideCellModel,
            background = roundedBackground,
            horizontalMargins = mainCellHorizontalMargin
        )
    }

    private fun createFooterCell(): TextViewCellModel {
        return TextViewCellModel.Raw(
            text = TextContainer(R.string.devices_footer_message),
            textAppearance = R.style.UiKit_TextAppearance_Regular_Label1,
            textColor = R.color.text_mountain,
            textSize = TextViewSize(textSize = 12f),
            badgeBackground = TextViewBackgroundModel(
                padding = badgePadding(left = 16.toPx(), right = 16.toPx(), top = 12.toPx(), bottom = 12.toPx())
            )
        )
    }
}
