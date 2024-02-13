package org.p2p.wallet.jupiter.ui.tokens.presenter

import androidx.annotation.StringRes
import java.math.BigDecimal
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.Constants
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatTokenWithSymbol
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.image.commonCircleImage
import org.p2p.uikit.utils.text.TextViewBackgroundModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.badgePadding
import org.p2p.uikit.utils.text.badgeRounded
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.utils.cutMiddle

/**
 * Contains base logic for mapping Swap domain models to Cell UI models
 * - sorting logic
 * - creating finance block cell model
 */
class SwapTokensCommonMapper(
    private val swapTokensRepository: JupiterSwapTokensRepository
) {
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

    suspend fun SwapTokenModel.toTokenFinanceCellModel(
        isPopularToken: Boolean = false,
        isSearchResult: Boolean = false
    ): MainCellModel = when (this) {
        is SwapTokenModel.JupiterToken -> asTokenFinanceCellModel(isPopularToken, isSearchResult)
        is SwapTokenModel.UserToken -> asTokenFinanceCellModel(isPopularToken, isSearchResult)
    }

    private suspend fun SwapTokenModel.UserToken.asTokenFinanceCellModel(
        isPopularToken: Boolean,
        isSearchResult: Boolean
    ): MainCellModel = with(details) {
        createTokenFinanceCellModel(
            tokenIconUrl = iconUrl.orEmpty(),
            tokenName = tokenName,
            tokenSymbol = tokenSymbol,
            tokenMint = mintAddress,
            tokenDecimals = decimals,
            totalTokenAmount = total,
            totalTokenPriceInUsd = totalInUsd?.formatFiat(),
            payload = SwapTokensCellModelPayload(
                hasPopularLabel = isPopularToken,
                isSearchResultItem = isSearchResult,
                tokenModel = this@asTokenFinanceCellModel
            ),
            addPopularLabel = isPopularToken,
            isStrictToken = null // don't have that info in user tokens
        )
    }

    private suspend fun SwapTokenModel.JupiterToken.asTokenFinanceCellModel(
        isPopularToken: Boolean,
        isSearchResult: Boolean
    ): MainCellModel =
        with(details) {
            createTokenFinanceCellModel(
                tokenIconUrl = iconUrl.orEmpty(),
                tokenName = tokenName,
                tokenSymbol = tokenSymbol,
                tokenMint = tokenMint.base58Value,
                tokenDecimals = decimals,
                totalTokenAmount = null,
                totalTokenPriceInUsd = null,
                addPopularLabel = isPopularToken,
                isStrictToken = isStrictToken,
                payload = SwapTokensCellModelPayload(
                    hasPopularLabel = isPopularToken,
                    isSearchResultItem = isSearchResult,
                    tokenModel = this@asTokenFinanceCellModel
                )
            )
        }

    private suspend fun createTokenFinanceCellModel(
        tokenIconUrl: String,
        tokenName: String,
        tokenSymbol: String,
        tokenMint: String,
        tokenDecimals: Int,
        totalTokenAmount: BigDecimal?,
        totalTokenPriceInUsd: String?,
        payload: SwapTokensCellModelPayload,
        isStrictToken: Boolean?,
        addPopularLabel: Boolean,
    ): MainCellModel {
        return MainCellModel(
            leftSideCellModel = createLeftSideModel(
                tokenIconUrl = tokenIconUrl,
                tokenName = tokenName,
                tokenSymbol = tokenSymbol,
                tokenMint = tokenMint,
                isStrictToken = isStrictToken,
            ),
            rightSideCellModel = if (addPopularLabel) {
                createPopularRightSideModel()
            } else {
                createPriceRightSideModel(
                    totalTokenAmount = totalTokenAmount,
                    tokenSymbol = tokenSymbol,
                    tokenDecimals = tokenDecimals,
                    totalTokenPriceInUsd = totalTokenPriceInUsd,
                )
            },
            payload = payload
        )
    }

    private suspend fun createLeftSideModel(
        tokenIconUrl: String,
        tokenName: String,
        tokenSymbol: String,
        tokenMint: String,
        isStrictToken: Boolean? // can be null for user tokens, we don't have info
    ): LeftSideCellModel.IconWithText {
        // the only way to know is token strict both
        // for JupiterToken and UserToken
        val isStrictToken = isStrictToken
            ?: swapTokensRepository.findTokenByMint(tokenMint.toBase58Instance())?.isStrictToken
            ?: true

        val tokenIconImage =
            DrawableContainer.Raw(iconUrl = tokenIconUrl)
                .let(::commonCircleImage)
                .let(IconWrapperCellModel::SingleIcon)

        val tokenNameTitle = buildString {
            append(tokenName)
            if (!isStrictToken) {
                append("️ ⚠")
            }
        }
        val firstLineText = TextViewCellModel.Raw(TextContainer.Raw(tokenNameTitle))

        val tokenAmountOrJustSymbol = "$tokenSymbol • ${tokenMint.cutMiddle(cutCount = 5)}"
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
                    background = badgeRounded(tint = R.color.elements_rain),
                    padding = badgePadding()
                )
            )
        )
    }

    private fun createPriceRightSideModel(
        totalTokenAmount: BigDecimal?,
        tokenSymbol: String,
        tokenDecimals: Int,
        totalTokenPriceInUsd: String?
    ): RightSideCellModel.TwoLineText? {
        val usdAmount = totalTokenPriceInUsd?.let {
            TextViewCellModel.Raw(
                TextContainer.Raw("${Constants.USD_SYMBOL} $it"),
            )
        }
        val tokenAmount = totalTokenAmount?.let {
            TextViewCellModel.Raw(
                TextContainer.Raw(it.formatTokenWithSymbol(tokenSymbol, tokenDecimals)),
            )
        }

        if (usdAmount == null && tokenAmount == null) return null

        return RightSideCellModel.TwoLineText(
            firstLineText = usdAmount,
            secondLineText = tokenAmount
        )
    }
}
