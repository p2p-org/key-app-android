package org.p2p.wallet.svl.model

import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.formatToken
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R

class ReceiveViaLinkMapper {

    fun mapTokenAmount(token: Token.Active): TextViewCellModel =
        TextViewCellModel.Raw(
            text = TextContainer("${token.total} ${token.tokenSymbol}")
        )

    fun mapSenderAddress(address: String): TextViewCellModel =
        TextViewCellModel.Raw(
            text = TextContainer(address)
        )

    fun mapTokenIcon(token: Token.Active): IconWrapperCellModel =
        IconWrapperCellModel.SingleIcon(
            icon = ImageViewCellModel(
                icon = DrawableContainer(token.iconUrl.orEmpty()),
                clippingShape = shapeCircle()
            )
        )

    fun mapClaimSuccessMessage(token: Token.Active): TextViewCellModel {
        return TextViewCellModel.Raw(
            text = TextContainer(
                R.string.send_via_link_receive_funds_success_title,
                token.total.formatToken(token.decimals), token.tokenSymbol
            )
        )
    }
}
