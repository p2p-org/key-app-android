package org.p2p.wallet.swap.ui.jupiter.settings.presenter

import androidx.annotation.StringRes
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R

class SwapCommonSettingsMapper {

    fun getNetworkFeeCell(): FinanceBlockCellModel {
        return FinanceBlockCellModel(
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_settings_network_fee_title),
                    ),
                    secondLineText = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_settings_network_fee_subtitle),
                        textColor = R.color.text_mint,
                    ),
                ),
                rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                    text = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_settings_network_fee_free),
                        textColor = R.color.text_mint,
                    ),
                    firstIcon = ImageViewCellModel(
                        icon = DrawableContainer(R.drawable.ic_info_outline),
                        iconTint = R.color.icons_mint,
                    )
                ),
                payload = SwapSettingsPayload.NETWORK_FEE,
            )
    }

    fun getSlippageList(slippage: Double):List<AnyCellItem> = mutableListOf<AnyCellItem>().apply {
        val selectedSlippage = slippage.slippageToEnum()
        addSlippageCell(
            slippage = TextContainer("0,1%"),
            isSelected = selectedSlippage == SwapSlippagePayload.ZERO_POINT_ONE,
            payload = SwapSlippagePayload.ZERO_POINT_ONE
        )
        addSlippageCell(
            slippage = TextContainer("0,5%"),
            isSelected = selectedSlippage == SwapSlippagePayload.ZERO_POINT_FIVE,
            payload = SwapSlippagePayload.ZERO_POINT_FIVE
        )
        addSlippageCell(
            slippage = TextContainer("1%"),
            isSelected = selectedSlippage == SwapSlippagePayload.ONE,
            payload = SwapSlippagePayload.ONE
        )
        addSlippageCell(
            slippage = TextContainer(R.string.swap_settings_slippage_custom),
            isSelected = selectedSlippage == SwapSlippagePayload.CUSTOM,
            payload = SwapSlippagePayload.CUSTOM
        )
    }

    private fun MutableList<AnyCellItem>.addSlippageCell(
        slippage: TextContainer,
        isSelected: Boolean,
        payload: SwapSlippagePayload,
    ) {
        add(
            FinanceBlockCellModel(
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
            )
        )
    }

    fun Double.slippageToEnum(): SwapSlippagePayload {
        return when (this) {
            0.1 -> SwapSlippagePayload.ZERO_POINT_ONE
            0.5 -> SwapSlippagePayload.ZERO_POINT_FIVE
            1.0 -> SwapSlippagePayload.ONE
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
