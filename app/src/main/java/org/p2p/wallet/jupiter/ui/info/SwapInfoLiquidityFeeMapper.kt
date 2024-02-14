package org.p2p.wallet.jupiter.ui.info

import androidx.annotation.DrawableRes
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.crypto.Base58String
import org.p2p.core.utils.asUsdSwap
import org.p2p.core.utils.formatTokenWithSymbol
import org.p2p.core.utils.fromLamports
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellStyle
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.info_block.InfoBlockCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoutePlanV6
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository

class SwapInfoLiquidityFeeMapper(
    private val swapTokensRepository: JupiterSwapTokensRepository,
    private val tokenServiceRepository: TokenServiceRepository
) {

    fun mapNoRouteLoaded(): List<AnyCellItem> {
        return listOf(createLiquidityFeeBanner())
    }

    fun mapLiquidityFees(
        route: JupiterSwapRouteV6,
    ): Flow<List<AnyCellItem>> = flow {
        // emit banner while we load
        emit(listOf(createLiquidityFeeBanner()))

        val keyAppFee = route.fees.platformFeeTokenB
        val tokenBMint = route.outTokenMint

        val cellModels = mutableListOf<AnyCellItem>()

        cellModels += createLiquidityFeeBanner()
        route.routePlans.forEach { routePlan ->
            cellModels += mapLiquidityFeeCellLoadingRate(routePlan)
        }

        val keyAppFeeCellModel = createKeyAppFeeCellNoRate(keyAppFee, tokenBMint)
        if (keyAppFeeCellModel != null) {
            cellModels += keyAppFeeCellModel
        }
        // emit info without rates
        emit(cellModels)

        // another collection, we can't modify cellModels
        val cellModelsWithRates = mutableListOf<AnyCellItem>()
        // start loading rates for liquidity fees
        cellModelsWithRates += createLiquidityFeeBanner()
        route.routePlans.forEach { routePlan ->
            val feeUsd = getUsdAmountForTokenAmount(routePlan.feeAmount, routePlan.feeMint)

            cellModelsWithRates += mapLiquidityFeeCellLoadingRate(routePlan)
                .copy(rightSideCellModel = feeUsd?.let(::createUsdRateCellModel))
        }

        val keyAppFeeUsd = getUsdAmountForTokenAmount(keyAppFee, tokenBMint)
        val updatedKeyAppFeeCell = createKeyAppFeeCellNoRate(keyAppFee, tokenBMint)?.copy(
            rightSideCellModel = keyAppFeeUsd?.let(::createUsdRateCellModel)
        )
        if (updatedKeyAppFeeCell != null) {
            cellModelsWithRates += updatedKeyAppFeeCell
        }
        // emit info with rates
        emit(cellModelsWithRates)
    }

    private suspend fun mapLiquidityFeeCellLoadingRate(
        routePlan: JupiterSwapRoutePlanV6,
    ): MainCellModel {
        val label = routePlan.label
        val liquidityToken = swapTokensRepository.findTokenByMint(routePlan.feeMint)
        val liquidityFee = routePlan.feeAmount

        val feePercent = "${routePlan.percent}%"
        val firstLineText = TextViewCellModel.Raw(
            text = TextContainer(R.string.swap_info_details_liquidity_cell_title, label, feePercent),
            maxLines = 2
        )

        val secondLineText = liquidityToken?.let {
            val feeInTokenLamports = liquidityFee
                .fromLamports(it.decimals)
                .formatTokenWithSymbol(it.tokenSymbol, it.decimals)
            TextViewCellModel.Raw(text = TextContainer(feeInTokenLamports))
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

    private suspend fun createKeyAppFeeCellNoRate(keyAppFee: BigInteger, tokenBMint: Base58String): MainCellModel? {
        val tokenB = swapTokensRepository.findTokenByMint(tokenBMint) ?: return null
        val feeAmount = keyAppFee.fromLamports(tokenB.decimals)
            .formatTokenWithSymbol(tokenB.tokenSymbol, tokenB.decimals)

        return MainCellModel(
            leftSideCellModel = LeftSideCellModel.IconWithText(
                firstLineText = TextViewCellModel.Raw(
                    TextContainer(R.string.swap_info_details_key_app_fee_title)
                ),
                secondLineText = TextViewCellModel.Raw(
                    TextContainer(feeAmount)
                )
            ),
            rightSideCellModel = rightSideSkeleton(),
            styleType = MainCellStyle.BASE_CELL,
        )
    }

    private fun createUsdRateCellModel(
        usdAmount: BigDecimal
    ): RightSideCellModel {
        return RightSideCellModel.SingleTextTwoIcon(
            text = TextViewCellModel.Raw(TextContainer(usdAmount.asUsdSwap()))
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

    private fun createLiquidityFeeBanner(): AnyCellItem = SwapInfoBannerCellModel(
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

    private fun getIcon(@DrawableRes icon: Int): IconWrapperCellModel.SingleIcon = IconWrapperCellModel.SingleIcon(
        icon = ImageViewCellModel(
            icon = DrawableContainer(icon),
            iconTint = R.color.icons_mountain,
            background = DrawableCellModel(tint = R.color.bg_smoke),
            clippingShape = shapeCircle(),
        )
    )

    private suspend fun getUsdAmountForTokenAmount(
        amount: BigInteger,
        tokenMint: Base58String,
    ): BigDecimal? {
        val token = swapTokensRepository.findTokenByMint(tokenMint) ?: return null
        val tokenRate = tokenServiceRepository.getTokenPriceByAddress(
            tokenAddress = tokenMint.base58Value,
            networkChain = TokenServiceNetwork.SOLANA
        )?.usdRate ?: return null

        return amount.fromLamports(token.decimals) * tokenRate
    }
}
