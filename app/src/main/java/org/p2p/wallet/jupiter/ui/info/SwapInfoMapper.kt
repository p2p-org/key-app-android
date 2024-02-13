package org.p2p.wallet.jupiter.ui.info

import androidx.annotation.DrawableRes
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.info_block.InfoBlockCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R

class SwapInfoMapper {

    fun mapNetworkFee(): List<AnyCellItem> = buildList {
        this += SwapInfoBannerCellModel(
            banner = R.drawable.ic_welcome,
            infoCell = InfoBlockCellModel(
                icon = getIcon(R.drawable.ic_lightning),
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_info_details_network_fee_title)
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_info_details_network_fee_subtitle)
                )
            )
        )
    }

    fun mapAccountFee(): List<AnyCellItem> = buildList {
        this += SwapInfoBannerCellModel(
            banner = R.drawable.ic_wallet_found,
            infoCell = InfoBlockCellModel(
                icon = getIcon(R.drawable.ic_lightning),
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_info_details_account_fee_title)
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_info_details_account_fee_subtitle)
                )
            )
        )
    }

    fun mapToken2022Fee(
        isTransferFee: Boolean,
    ): List<AnyCellItem> {
        val title = if (isTransferFee) {
            R.string.swap_info_details_transfer_fee_title
        } else {
            R.string.swap_info_details_interest_fee_title
        }
        // used same for interest and transfer fee
        val subtitle = R.string.swap_info_details_transfer_fee_subtitle
        return listOf(
            SwapInfoBannerCellModel(
                banner = R.drawable.ic_wallet_found,
                infoCell = InfoBlockCellModel(
                    icon = getIcon(R.drawable.ic_lightning),
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(title)
                    ),
                    secondLineText = TextViewCellModel.Raw(
                        text = TextContainer(subtitle)
                    )
                )
            )
        )
    }

    fun mapMinimumReceived(): List<AnyCellItem> = buildList {
        this += SwapInfoBannerCellModel(
            banner = R.drawable.ic_about_earn_3,
            infoCell = InfoBlockCellModel(
                icon = IconWrapperCellModel.SingleIcon(
                    icon = ImageViewCellModel(
                        icon = DrawableContainer(R.drawable.ic_lightning),
                        iconTint = R.color.icons_mountain,
                        background = DrawableCellModel(tint = R.color.bg_snow),
                        clippingShape = shapeCircle(),
                    )
                ),
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_info_details_minimum_received_fee_title)
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_info_details_minimum_received_fee_subtitle)
                ),
                background = DrawableCellModel(
                    drawable = shapeDrawable(shapeRoundedAll(12f.toPx())),
                    tint = org.p2p.uikit.R.color.bg_smoke
                )
            )
        )
    }

    private fun getIcon(@DrawableRes icon: Int): IconWrapperCellModel.SingleIcon = IconWrapperCellModel.SingleIcon(
        icon = ImageViewCellModel(
            icon = DrawableContainer(icon),
            iconTint = R.color.icons_mountain,
            background = DrawableCellModel(tint = R.color.bg_smoke),
            clippingShape = shapeCircle(),
        )
    )
}
