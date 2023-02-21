package org.p2p.wallet.swap.ui.jupiter.tokens.presenter

import androidx.annotation.StringRes
import androidx.core.net.toUri
import android.net.Uri
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
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
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.utils.Base58String

class SwapTokensMapper {
    fun toCellItems(
        currentSelectedTokenMint: Base58String,
        userTokens: List<Token.Active>,
        swapTokens: List<JupiterSwapToken>
    ): List<AnyCellItem> = buildList {
        addAll(
            chosenTokenGroup(
                tokenIconUrl = (
                    "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/" +
                        "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v/logo.png"
                    )
                    .toUri(),
                tokenName = "USDC",
                totalTokenAmount = null,
                totalTokenPriceInUsd = null
            )
        )

        addAll(userTokensGroup(emptyList()))
        addAll(allOtherTokens(emptyList()))
    }

    private fun chosenTokenGroup(
        tokenIconUrl: Uri,
        tokenName: String,
        totalTokenAmount: String?,
        totalTokenPriceInUsd: String?
    ): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_chosen_token)

        val chosenToken = createTokenFinanceCellModel(
            tokenIconUrl = tokenIconUrl,
            tokenName = tokenName,
            totalTokenAmount = totalTokenAmount,
            totalTokenPriceInUsd = totalTokenPriceInUsd
        )

        add(sectionHeader)
        add(chosenToken)
    }

    private fun userTokensGroup(userTokens: List<Token.Active>): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_chosen_token)
        val userTokenItems = userTokens.map {
            createTokenFinanceCellModel(
                tokenIconUrl = it.iconUrl.orEmpty().toUri(),
                tokenName = it.tokenName,
                totalTokenAmount = it.getFormattedTotal(includeSymbol = true),
                totalTokenPriceInUsd = it.getFormattedUsdTotal()
            )
        }

        add(sectionHeader)
        addAll(userTokenItems)
    }

    private fun allOtherTokens(otherTokens: List<JupiterSwapToken>): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_all_tokens)
        val userTokenItems = otherTokens.map {
            createTokenFinanceCellModel(
                tokenIconUrl = it.logoUri.orEmpty().toUri(),
                tokenName = it.tokenName,
                totalTokenAmount = null,
                totalTokenPriceInUsd = null
            )
        }

        add(sectionHeader)
        addAll(userTokenItems)
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
