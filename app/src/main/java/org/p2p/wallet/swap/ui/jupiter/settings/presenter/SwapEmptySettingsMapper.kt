package org.p2p.wallet.swap.ui.jupiter.settings.presenter

import java.math.BigDecimal
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.formatToken
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.finance_block.FinanceBlockStyle
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel

class SwapEmptySettingsMapper(
    private val commonMapper: SwapCommonSettingsMapper
) {

    fun mapEmptyList(
        slippage: Double,
        tokenB: SwapTokenModel,
    ): List<AnyCellItem> = buildList {
        this += commonMapper.getNetworkFeeCell()
        addMinimumReceivedCell(tokenB)
        this += commonMapper.createHeader(R.string.swap_settings_slippage_title)
        this += commonMapper.getSlippageList(slippage)
    }

    private fun MutableList<AnyCellItem>.addMinimumReceivedCell(tokenB: SwapTokenModel, amount: BigDecimal? = null) {
        this += FinanceBlockCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_settings_minimum_received_title),
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(
                        R.string.swap_settings_minimum_received_subtitle,
                        amount?.formatToken(tokenB.decimals) ?: "0",
                        tokenB.tokenSymbol
                    ),
                ),
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_info_outline),
                    iconTint = R.color.icons_mountain,
                )
            ),
            payload = SwapSettingsPayload.MINIMUM_RECEIVED,
            styleType = FinanceBlockStyle.BASE_CELL,
        )
    }
}
