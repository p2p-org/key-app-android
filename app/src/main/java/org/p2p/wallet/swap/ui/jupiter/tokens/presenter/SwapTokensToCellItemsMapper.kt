package org.p2p.wallet.swap.ui.jupiter.tokens.presenter

import androidx.annotation.StringRes
import androidx.core.net.toUri
import android.net.Uri
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.Constants
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.utils.image.commonCircleImage
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.utils.compareTo

class SwapTokensToCellItemsMapper {

    private val userTokensSorter = Comparator<SwapTokenModel.UserToken> { current, next ->
        val currentTokenMint = current.mintAddress.base58Value
        val nextTokenMint = next.mintAddress.base58Value

        val isCurrentUsdc = currentTokenMint.contentEquals(Constants.USDC_MINT, ignoreCase = true)
        val isNextUsdc = nextTokenMint.contentEquals(Constants.USDC_MINT, ignoreCase = true)

        val isCurrentUsdt = currentTokenMint.contentEquals(Constants.USDT_MINT, ignoreCase = true)
        val isNextUsdt = nextTokenMint.contentEquals(Constants.USDT_MINT, ignoreCase = true)

        when {
            isCurrentUsdc -> 1
            isNextUsdc -> -1
            isCurrentUsdt -> 1
            isNextUsdt -> -1
            else -> current.tokenAmountInUsd.compareTo(next.tokenAmountInUsd)
        }
    }

    private val otherTokensSorter = Comparator<SwapTokenModel.JupiterToken> { current, next ->
        current.details.tokenName.compareTo(next.details.tokenName)
    }

    fun toCellItems(
        chosenToken: SwapTokenModel,
        swapTokens: List<SwapTokenModel>
    ): List<AnyCellItem> = buildList {
        this += chosenTokenGroup(chosenToken)

        this += userTokensGroup(swapTokens.filterIsInstance<SwapTokenModel.UserToken>())
        this += allOtherTokens(swapTokens.filterIsInstance<SwapTokenModel.JupiterToken>())
    }

    private fun chosenTokenGroup(
        chosenToken: SwapTokenModel
    ): List<AnyCellItem> = buildList {
        val sectionHeaderCell = createSectionHeader(R.string.swap_tokens_section_chosen_token)
        val chosenTokenCell = when (chosenToken) {
            is SwapTokenModel.JupiterToken -> {
                createTokenFinanceCellModel(
                    tokenIconUrl = chosenToken.iconUrl.orEmpty().toUri(),
                    tokenName = chosenToken.details.tokenName,
                    totalTokenAmount = null,
                    totalTokenPriceInUsd = null
                )
            }
            is SwapTokenModel.UserToken -> {
                createTokenFinanceCellModel(
                    tokenIconUrl = chosenToken.details.iconUrl.orEmpty().toUri(),
                    tokenName = chosenToken.details.tokenName,
                    totalTokenAmount = chosenToken.details.getFormattedTotal(includeSymbol = true),
                    totalTokenPriceInUsd = chosenToken.details.getFormattedUsdTotal()
                )
            }
        }

        this += sectionHeaderCell
        this += chosenTokenCell
    }

    private fun userTokensGroup(userTokens: List<SwapTokenModel.UserToken>): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_chosen_token)
        val userTokenItems = userTokens
            .sortedWith(userTokensSorter)
            .map {
                createTokenFinanceCellModel(
                    tokenIconUrl = it.details.iconUrl.orEmpty().toUri(),
                    tokenName = it.details.tokenName,
                    totalTokenAmount = it.details.getFormattedTotal(includeSymbol = true),
                    totalTokenPriceInUsd = it.details.getFormattedUsdTotal()
                )
            }

        this += sectionHeader
        this += userTokenItems
    }

    private fun allOtherTokens(otherTokens: List<SwapTokenModel.JupiterToken>): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_all_tokens)
        val otherTokenItems = otherTokens
            .sortedWith(otherTokensSorter)
            .map {
                createTokenFinanceCellModel(
                    tokenIconUrl = it.details.logoUri.orEmpty().toUri(),
                    tokenName = it.details.tokenName,
                    totalTokenAmount = null,
                    totalTokenPriceInUsd = null
                )
            }

        this += sectionHeader
        this += otherTokenItems
    }

    private fun createSectionHeader(@StringRes stringRes: Int): SectionHeaderCellModel {
        return SectionHeaderCellModel(
            sectionTitle = TextContainer(stringRes),
            isShevronVisible = false
        )
    }

    private fun createTokenFinanceCellModel(
        tokenIconUrl: Uri,
        tokenName: String,
        totalTokenAmount: String?,
        totalTokenPriceInUsd: String?
    ): FinanceBlockCellModel {
        return FinanceBlockCellModel(
            leftSideCellModel = createLeftSideModel(
                tokenIconUrl = tokenIconUrl,
                tokenName = tokenName,
                totalTokenAmount = totalTokenAmount
            ),
            rightSideCellModel = createRightSideModel(totalTokenPriceInUsd)
        )
    }

    private fun createLeftSideModel(
        tokenIconUrl: Uri,
        tokenName: String,
        totalTokenAmount: String?
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
        val symbolOrAmountText = totalTokenAmount?.let { "$totalTokenAmount $tokenName" } ?: tokenName
        val tokenSymbolWithAmount = TextViewCellModel.Raw(
            TextContainer.Raw(symbolOrAmountText),
            textAppearance = R.style.UiKit_TextAppearance_Regular_Label1,
            textColor = R.color.text_mountain
        )

        return LeftSideCellModel.IconWithText(
            icon = tokenIconImage,
            firstLineText = tokenNameText,
            secondLineText = tokenSymbolWithAmount
        )
    }

    private fun createRightSideModel(totalTokenPriceInUsd: String?): RightSideCellModel.TwoLineText? {
        if (totalTokenPriceInUsd == null) return null

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
