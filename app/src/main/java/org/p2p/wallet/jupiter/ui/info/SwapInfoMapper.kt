package org.p2p.wallet.jupiter.ui.info

import androidx.annotation.DrawableRes
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.asUsdSwap
import org.p2p.core.utils.fromLamports
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellStyle
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
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoutePlanV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.ui.main.SwapRateLoaderState

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

    fun mapLoadingLiquidityFee(
        allTokens: List<SwapTokenModel>,
        route: JupiterSwapRouteV6? = null,
    ): List<AnyCellItem> = buildList {
        addAll(mapEmptyLiquidityFee())
        if (route == null) return@buildList

        route.routePlans.forEach { routePlan ->
            this += getLiquidityFeeCell(routePlan, allTokens)
        }
    }

    fun getLiquidityFeeCell(
        routePlan: JupiterSwapRoutePlanV6,
        allTokens: List<SwapTokenModel>
    ): MainCellModel {
        val label = routePlan.label
        val liquidityToken = allTokens.find { routePlan.feeMint == it.mintAddress }
        val liquidityFee = routePlan.feeAmount

        val feePercent = routePlan.percent + "%"
        val firstLineText = TextViewCellModel.Raw(
            text = TextContainer(R.string.swap_info_details_liquidity_cell_title, label, feePercent),
            maxLines = 2
        )

        val secondLineText = liquidityToken?.let {
            val feeInTokenLamports = liquidityFee.fromLamports(it.decimals)
            TextViewCellModel.Raw(
                text = TextContainer("$feeInTokenLamports ${it.tokenSymbol}")
            )
        }

        return MainCellModel(
            styleType = MainCellStyle.BASE_CELL,
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = firstLineText,
                secondLineText = secondLineText
            ),
            rightSideCellModel = rightSideSkeleton(),
        )
    }

    private fun rightSideSkeleton(): RightSideCellModel.SingleTextTwoIcon = RightSideCellModel.SingleTextTwoIcon(
        text = TextViewCellModel.Skeleton(
            skeleton = SkeletonCellModel(
                height = 20.toPx(),
                width = 60.toPx(),
                radius = 5f.toPx(),
            ),
        ),
    )

    fun mapEmptyLiquidityFee(): List<AnyCellItem> = buildList {
        this += SwapInfoBannerCellModel(
            banner = R.drawable.ic_fee_banner,
            infoCell = InfoBlockCellModel(
                icon = getIcon(R.drawable.ic_lightning),
                firstLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_info_details_liquidity_fee_title)
                ),
                secondLineText = TextViewCellModel.Raw(
                    text = TextContainer(R.string.swap_info_details_liquidity_fee_subtitle)
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

    fun updateLiquidityFee(
        marketInfo: JupiterSwapRoutePlanV6,
        oldCell: MainCellModel,
        state: SwapRateLoaderState
    ): MainCellModel {
        return when (state) {
            SwapRateLoaderState.Empty -> oldCell
            is SwapRateLoaderState.NoRateAvailable,
            is SwapRateLoaderState.Error -> oldCell.copy(rightSideCellModel = null)
            SwapRateLoaderState.Loading -> oldCell.copy(rightSideCellModel = rightSideSkeleton())
            is SwapRateLoaderState.Loaded -> {
                val rate = state.rate
                val token = state.token
                val feeInUsd = marketInfo.feeAmount
                    .fromLamports(token.decimals)
                    .multiply(rate)
                    .asUsdSwap()
                oldCell.copy(
                    rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                        text = TextViewCellModel.Raw(
                            text = TextContainer(feeInUsd)
                        ),
                    )
                )
            }
        }
    }
}
