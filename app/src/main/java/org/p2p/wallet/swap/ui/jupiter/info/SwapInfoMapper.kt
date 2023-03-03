package org.p2p.wallet.swap.ui.jupiter.info

import androidx.annotation.DrawableRes
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.fromLamports
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.finance_block.FinanceBlockStyle
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.info_block.InfoBlockCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.ui.jupiter.settings.presenter.SwapCommonSettingsMapper

class SwapInfoMapper constructor(
    private val swapStateManager: SwapStateManager,
    private val commonMapper: SwapCommonSettingsMapper,
) {

    fun mapNetworkFee(): List<AnyCellItem> = buildList {
        this += SwapInfoBannerCellModel(banner = R.drawable.ic_welcome)
        this += InfoBlockCellModel(
            icon = getIcon(R.drawable.ic_lightning),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.swap_info_details_network_fee_title)
            ),
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.swap_info_details_network_fee_subtitle)
            )
        )
    }

    fun mapAccountFee(): List<AnyCellItem> = buildList {
        this += SwapInfoBannerCellModel(banner = R.drawable.ic_wallet_found)
        this += InfoBlockCellModel(
            icon = getIcon(R.drawable.ic_lightning),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.swap_info_details_account_fee_title)
            ),
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.swap_info_details_account_fee_subtitle)
            )
        )
    }

    suspend fun mapLiquidityFee(
        allTokens: List<SwapTokenModel>,
        route: JupiterSwapRoute? = null,
    ): List<AnyCellItem> = buildList {
        this += SwapInfoBannerCellModel(banner = R.drawable.ic_wallet_found)
        this += InfoBlockCellModel(
            icon = getIcon(R.drawable.ic_lightning),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.swap_info_details_liquidity_fee_title)
            ),
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.swap_info_details_liquidity_fee_subtitle)
            )
        )
        if (route == null) return@buildList

        route.marketInfos.forEach { marketInfo ->
            val label = marketInfo.label
            val lpToken = allTokens.find { marketInfo.lpFee.mint == it.mintAddress }
            var secondLineText: TextViewCellModel.Raw? = null
            val feeLamports = lpToken?.decimals?.let { marketInfo.lpFee.amountInLamports.fromLamports() }
            if (lpToken != null) {
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer("$feeLamports, ${lpToken.tokenSymbol}")
                )
            }

            val feePct = marketInfo.lpFee.pct.toPlainString() + "%"

            this += FinanceBlockCellModel(
                styleType = FinanceBlockStyle.BASE_CELL,
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(R.string.swap_info_details_liquidity_cell_title, label, feePct)
                    ),
                    secondLineText = secondLineText
                ),
                rightSideCellModel = RightSideCellModel.TwoLineText(
                    firstLineText = TextViewCellModel.Skeleton(
                        skeleton = SkeletonCellModel(
                            height = 20.toPx(),
                            width = 60.toPx(),
                            radius = 5f.toPx(),
                        ),
                    ),
                    secondLineText = secondLineText
                )
            )
        }
    }

    fun mapMinimumReceived(): List<AnyCellItem> = buildList {
        this += SwapInfoBannerCellModel(banner = R.drawable.ic_about_earn_3)
        this += InfoBlockCellModel(
            icon = IconWrapperCellModel.SingleIcon(
                icon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_lightning),
                    iconTint = R.color.icons_mountain,
                    background = DrawableCellModel(tint = R.color.bg_snow),
                    clippingShape = shapeCircle(),
                )
            ),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.swap_info_details_account_fee_title)
            ),
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(R.string.swap_info_details_account_fee_subtitle)
            ),
            background = DrawableCellModel(
                drawable = shapeDrawable(shapeRoundedAll(12f.toPx())),
                tint = org.p2p.uikit.R.color.bg_smoke
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
