package org.p2p.wallet.jupiter.ui.tokens.presenter

import androidx.annotation.StringRes
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.Constants
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.image.commonCircleImage
import org.p2p.uikit.utils.text.TextViewBackgroundModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.badgeRounded
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.utils.Base58String

/**
 * Contains base logic for mapping Swap domain models to Cell UI models
 * - sorting logic
 * - creating finance block cell model
 */
class SwapTokensCommonMapper {
    val byTokenMintComparator = Comparator<Base58String> { current, next ->
        val currentTokenMint = current.base58Value
        val nextTokenMint = next.base58Value

        val isCurrentUsdc = currentTokenMint.contentEquals(Constants.USDC_MINT, ignoreCase = true)
        val isNextUsdc = nextTokenMint.contentEquals(Constants.USDC_MINT, ignoreCase = true)

        val isCurrentUsdt = currentTokenMint.contentEquals(Constants.USDT_MINT, ignoreCase = true)
        val isNextUsdt = nextTokenMint.contentEquals(Constants.USDT_MINT, ignoreCase = true)

        when {
            isCurrentUsdc -> 1
            isNextUsdc -> -1
            isCurrentUsdt -> 1
            isNextUsdt -> -1
            else -> 0
        }
    }

    val otherTokensSorter: Comparator<SwapTokenModel> =
        compareBy(SwapTokenModel::tokenName)

    fun createSectionHeader(@StringRes stringRes: Int): SectionHeaderCellModel {
        return SectionHeaderCellModel(
            sectionTitle = TextContainer(stringRes),
            isShevronVisible = false
        )
    }

    fun SwapTokenModel.isSelectedToken(selectedToken: SwapTokenModel): Boolean {
        return this.equalsByMint(selectedToken)
    }

    fun SwapTokenModel.toTokenFinanceCellModel(
        isPopularToken: Boolean = false,
        isSearchResult: Boolean = false
    ): FinanceBlockCellModel = when (this) {
        is SwapTokenModel.JupiterToken -> asTokenFinanceCellModel(isPopularToken, isSearchResult)
        is SwapTokenModel.UserToken -> asTokenFinanceCellModel(isPopularToken, isSearchResult)
    }

    private fun SwapTokenModel.UserToken.asTokenFinanceCellModel(
        isPopularToken: Boolean,
        isSearchResult: Boolean
    ): FinanceBlockCellModel =
        with(details) {
            createTokenFinanceCellModel(
                tokenIconUrl = iconUrl.orEmpty(),
                tokenName = tokenName,
                tokenSymbol = tokenSymbol,
                totalTokenAmount = getFormattedTotal(),
                totalTokenPriceInUsd = totalInUsd?.formatFiat(),
                addPopularLabel = isPopularToken,
                payload = SwapTokensCellModelPayload(
                    hasPopularLabel = isPopularToken,
                    isSearchResultItem = isSearchResult,
                    tokenModel = this@asTokenFinanceCellModel
                )
            )
        }

    private fun SwapTokenModel.JupiterToken.asTokenFinanceCellModel(
        isPopularToken: Boolean,
        isSearchResult: Boolean
    ): FinanceBlockCellModel =
        with(details) {
            createTokenFinanceCellModel(
                tokenIconUrl = iconUrl.orEmpty(),
                tokenName = tokenName,
                tokenSymbol = tokenSymbol,
                totalTokenAmount = null,
                totalTokenPriceInUsd = null,
                addPopularLabel = isPopularToken,
                payload = SwapTokensCellModelPayload(
                    hasPopularLabel = isPopularToken,
                    isSearchResultItem = isSearchResult,
                    tokenModel = this@asTokenFinanceCellModel
                )
            )
        }

    private fun createTokenFinanceCellModel(
        tokenIconUrl: String,
        tokenName: String,
        tokenSymbol: String,
        totalTokenAmount: String?,
        totalTokenPriceInUsd: String?,
        payload: SwapTokensCellModelPayload,
        addPopularLabel: Boolean,
    ): FinanceBlockCellModel {
        return FinanceBlockCellModel(
            leftSideCellModel = createLeftSideModel(
                tokenIconUrl = tokenIconUrl,
                tokenName = tokenName,
                tokenSymbol = tokenSymbol,
                totalTokenAmount = totalTokenAmount
            ),
            rightSideCellModel = if (addPopularLabel) {
                createPopularRightSideModel()
            } else {
                createPriceRightSideModel(totalTokenPriceInUsd)
            },
            payload = payload
        )
    }

    private fun createLeftSideModel(
        tokenIconUrl: String,
        tokenName: String,
        tokenSymbol: String,
        totalTokenAmount: String?,
    ): LeftSideCellModel.IconWithText {
        val tokenIconImage =
            DrawableContainer.Raw(iconUrl = tokenIconUrl)
                .let(::commonCircleImage)
                .let(IconWrapperCellModel::SingleIcon)

        val firstLineText = TextViewCellModel.Raw(TextContainer.Raw(tokenName))
        val tokenAmountOrJustSymbol = totalTokenAmount?.let { "$it $tokenSymbol" } ?: tokenSymbol
        val secondLineText = TextViewCellModel.Raw(TextContainer.Raw(tokenAmountOrJustSymbol))

        return LeftSideCellModel.IconWithText(
            icon = tokenIconImage,
            firstLineText = firstLineText,
            secondLineText = secondLineText
        )
    }

    private fun createPopularRightSideModel(): RightSideCellModel.SingleTextTwoIcon {
        return RightSideCellModel.SingleTextTwoIcon(
            text = TextViewCellModel.Raw(
                text = TextContainer(R.string.swap_tokens_popular_label),
                badgeBackground = TextViewBackgroundModel(
                    badgeRounded(tint = R.color.elements_rain)
                )
            ),
        )
    }

    private fun createPriceRightSideModel(totalTokenPriceInUsd: String?): RightSideCellModel.TwoLineText? {
        if (totalTokenPriceInUsd == null) return null

        val totalTokenInUsdText = TextViewCellModel.Raw(
            TextContainer.Raw("${Constants.USD_SYMBOL} $totalTokenPriceInUsd"),
        )
        return RightSideCellModel.TwoLineText(
            firstLineText = totalTokenInUsdText,
            secondLineText = null
        )
    }
}
