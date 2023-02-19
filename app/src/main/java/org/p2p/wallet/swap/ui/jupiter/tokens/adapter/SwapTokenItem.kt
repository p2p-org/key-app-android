package org.p2p.wallet.swap.ui.jupiter.tokens.adapter

import android.net.Uri
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.Constants
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.image.commonCircleImage
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R

sealed interface SwapTokenItem {
    class TokenSectionHeader(val sectionName: String) : SwapTokenItem {
        val model = SectionHeaderCellModel(
            sectionTitle = TextViewCellModel.Raw(text = TextContainer(sectionName)),
            isShevronVisible = false
        )
    }

    class SwapTokenFinanceBlock(
        tokenIconUrl: Uri,
        tokenName: String,
        totalTokenPriceInUsd: String,
        totalTokenAmount: String
    ) : SwapTokenItem {
        val model: FinanceBlockCellModel = FinanceBlockCellModel(
            leftSideCellModel = createLeftSideModel(
                tokenIconUrl = tokenIconUrl,
                tokenName = tokenName,
                totalTokenAmount = totalTokenAmount
            ),
            rightSideCellModel = createRightSideModel(totalTokenPriceInUsd)
        )

        private fun createLeftSideModel(
            tokenIconUrl: Uri,
            tokenName: String,
            totalTokenAmount: String
        ): LeftSideCellModel.IconWithText {
            val tokenIconImage =
                DrawableContainer.Raw(iconUrl = tokenIconUrl.toString())
                    .let(::commonCircleImage)
                    .let(IconWrapperCellModel::SingleIcon)

            val tokenNameText = TextViewCellModel.Raw(
                TextContainer.Raw(tokenName),
                textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                textColor = R.color.text_night
            )
            val tokenTotalAmountText = TextViewCellModel.Raw(
                TextContainer.Raw("$totalTokenAmount $tokenName"),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Label1,
                textColor = R.color.text_mountain
            )

            return LeftSideCellModel.IconWithText(
                icon = tokenIconImage,
                firstLineText = tokenNameText,
                secondLineText = tokenTotalAmountText
            )
        }

        private fun createRightSideModel(totalTokenPriceInUsd: String): RightSideCellModel.TwoLineText {
            val totalTokenInUsdText = TextViewCellModel.Raw(
                TextContainer.Raw("${Constants.USD_SYMBOL} $totalTokenPriceInUsd"),
                textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                textColor = R.color.text_night
            )
            return RightSideCellModel.TwoLineText(
                firstLineText = totalTokenInUsdText,
                secondLineText = null
            )
        }
    }
}
