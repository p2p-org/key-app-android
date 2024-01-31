package org.p2p.wallet.jupiter.ui.settings.presenter

import androidx.annotation.StringRes
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellStyle
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.ui.settings.view.SwapCustomSlippageCellModel
import org.p2p.wallet.swap.model.Slippage

class SwapCommonSettingsMapper {
    fun mapSlippageList(slippage: Slippage, isSelectedCustom: Boolean): List<AnyCellItem> = buildList {
        this += createHeader(R.string.swap_settings_slippage_title)
        this += getSlippageList(slippage, isSelectedCustom)
    }

    private fun getSlippageList(slippage: Slippage, isSelectedCustom: Boolean): List<AnyCellItem> = buildList {
        val selectedSlippage = slippage.slippageToEnum()
        addSlippageCell(
            slippage = TextContainer("0,1%"),
            isSelected = !isSelectedCustom && slippage is Slippage.Min,
            payload = SwapSlippagePayload.ZERO_POINT_ONE
        )
        addSlippageCell(
            slippage = TextContainer("0,5%"),
            isSelected = !isSelectedCustom && slippage is Slippage.Medium,
            payload = SwapSlippagePayload.ZERO_POINT_FIVE
        )
        addSlippageCell(
            slippage = TextContainer("1%"),
            isSelected = !isSelectedCustom && slippage is Slippage.One,
            payload = SwapSlippagePayload.ONE
        )
        val isCustom = isSelectedCustom || slippage is Slippage.Custom
        addSlippageCell(
            slippage = TextContainer(R.string.swap_settings_slippage_custom),
            isSelected = isCustom,
            payload = SwapSlippagePayload.CUSTOM
        )
        if (isCustom) {
            this += SwapCustomSlippageCellModel(slippage = slippage)
        }
    }

    private fun MutableList<AnyCellItem>.addSlippageCell(
        slippage: TextContainer,
        isSelected: Boolean,
        payload: SwapSlippagePayload,
    ) {
        add(
            MainCellModel(
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = TextViewCellModel.Raw(
                        text = slippage,
                    ),
                ),
                rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                    firstIcon = if (isSelected) {
                        ImageViewCellModel(
                            icon = DrawableContainer(R.drawable.ic_check),
                            iconTint = R.color.icons_mountain,
                        )
                    } else {
                        null
                    }
                ),
                payload = payload,
                styleType = MainCellStyle.BASE_CELL,
            )
        )
    }

    private fun Slippage.slippageToEnum(): SwapSlippagePayload {
        return when (this) {
            Slippage.Min -> SwapSlippagePayload.ZERO_POINT_ONE
            Slippage.Medium -> SwapSlippagePayload.ZERO_POINT_FIVE
            Slippage.One -> SwapSlippagePayload.ONE
            else -> SwapSlippagePayload.CUSTOM
        }
    }

    fun SwapSlippagePayload.slippageEnumToAmount(): Double? {
        return when (this) {
            SwapSlippagePayload.ZERO_POINT_ONE -> 0.1
            SwapSlippagePayload.ZERO_POINT_FIVE -> 0.5
            SwapSlippagePayload.ONE -> 1.0
            else -> null
        }
    }

    fun createHeader(@StringRes stringRes: Int): SectionHeaderCellModel {
        return SectionHeaderCellModel(
            sectionTitle = TextContainer(stringRes),
            isShevronVisible = false
        )
    }
}
