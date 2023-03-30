package org.p2p.wallet.svl.model

import android.content.Context
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.utils.DateTimeUtils

class ReceiveViaLinkMapper {

    fun mapTokenAmount(token: Token.Active): TextViewCellModel =
        TextViewCellModel.Raw(
            text = TextContainer("${token.total} ${token.tokenSymbol}")
        )

    fun mapSenderAddress(address: String): TextViewCellModel =
        TextViewCellModel.Raw(
            text = TextContainer(address)
        )

    fun mapCurrentDate(context: Context): TextViewCellModel {
        val dateText = DateTimeUtils.getDateFormatted(System.currentTimeMillis(), context)
        return TextViewCellModel.Raw(
            text = TextContainer(dateText)
        )
    }

    fun mapTokenIcon(token: Token.Active): ImageViewCellModel =
        ImageViewCellModel(
            icon = DrawableContainer(token.iconUrl.orEmpty())
        )

    fun mapClaimSuccessMessage(context: Context, token: Token.Active): TextViewCellModel {
        val text = context.getString(
            R.string.send_via_link_receive_funds_success_title,
            token.total,
            token.tokenSymbol
        )
        return TextViewCellModel.Raw(
            text = TextContainer(text)
        )
    }
}
