package org.p2p.wallet.receive.tokenselect

import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.token.TokenData
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.icon_wrapper.TwoIconAngle
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.image.commonCircleImage
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.receive.tokenselect.models.ReceiveTokenPayload

object ReceiveTokensMapper {

    fun TokenData.toTokenFinanceCellModel(
        solTokenUrl: String,
        ethTokenUrl: String
    ): AnyCellItem {
        val isErc20Token = ERC20Tokens.findTokenByMint(mintAddress) != null
        return FinanceBlockCellModel(
            leftSideCellModel = createLeftSideModel(
                tokenIconUrl = iconUrl.orEmpty(),
                tokenName = name,
                tokenSymbol = symbol,
            ),
            rightSideCellModel = createRightSideModel(
                firstIconUrl = solTokenUrl,
                secondIconUrl = ethTokenUrl,
                isErc20Token = isErc20Token
            ),
            payload = ReceiveTokenPayload(
                tokenData = this,
                isErc20Token = isErc20Token
            )
        )
    }

    private fun createLeftSideModel(
        tokenIconUrl: String,
        tokenName: String,
        tokenSymbol: String
    ): LeftSideCellModel.IconWithText {
        val tokenIconImage =
            DrawableContainer.Raw(iconUrl = tokenIconUrl)
                .let(::commonCircleImage)
                .let(IconWrapperCellModel::SingleIcon)

        val firstLineText = TextViewCellModel.Raw(
            text = TextContainer.Raw(tokenName),
            textAppearance = R.style.UiKit_TextAppearance_Regular_Text3
        )
        val secondLineText = TextViewCellModel.Raw(TextContainer.Raw(tokenSymbol))

        return LeftSideCellModel.IconWithText(
            icon = tokenIconImage,
            firstLineText = firstLineText,
            secondLineText = secondLineText
        )
    }

    private fun createRightSideModel(
        firstIconUrl: String,
        secondIconUrl: String,
        isErc20Token: Boolean
    ): RightSideCellModel {
        val solImageCell = ImageViewCellModel(
            icon = DrawableContainer.Raw(firstIconUrl),
            clippingShape = shapeCircle(),
        )
        val firstIcon: ImageViewCellModel?
        val secondIcon: ImageViewCellModel?
        if (isErc20Token) {
            firstIcon = ImageViewCellModel(
                icon = DrawableContainer.Raw(secondIconUrl),
                background = DrawableCellModel(
                    drawable = shapeDrawable(shape = shapeCircle()),
                    tint = R.color.icons_rain,
                ),
                clippingShape = shapeCircle(),
            )
            secondIcon = solImageCell
        } else {
            firstIcon = solImageCell
            secondIcon = null
        }
        return RightSideCellModel.IconWrapper(
            iconWrapper = IconWrapperCellModel.TwoIcon(
                first = firstIcon,
                second = secondIcon,
                angleType = TwoIconAngle.Plus180
            )
        )
    }
}
