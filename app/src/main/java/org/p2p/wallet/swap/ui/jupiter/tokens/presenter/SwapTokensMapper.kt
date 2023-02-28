package org.p2p.wallet.swap.ui.jupiter.tokens.presenter

import org.p2p.core.utils.orZero
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.R
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel

class SwapTokensMapper : SwapTokensPresenterMapper() {

    private val userTokensSorter: Comparator<SwapTokenModel.UserToken> =
        compareByDescending(byTokenMintComparator, SwapTokenModel.UserToken::mintAddress)
            .thenByDescending { it.tokenAmountInUsd.orZero() }

    private val otherTokensSorter: Comparator<SwapTokenModel.JupiterToken> =
        compareByDescending(SwapTokenModel.JupiterToken::tokenName)

    fun toCellItems(
        chosenToken: SwapTokenModel,
        swapTokens: List<SwapTokenModel>
    ): List<AnyCellItem> = buildList {
        this += chosenTokenGroup(chosenToken)

        this += userTokensGroup(swapTokens.filterIsInstance<SwapTokenModel.UserToken>())
        this += allOtherTokensGroup(swapTokens.filterIsInstance<SwapTokenModel.JupiterToken>())
    }

    private fun chosenTokenGroup(
        selectedTokenModel: SwapTokenModel
    ): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_chosen_token)
        val selectedToken = selectedTokenModel.toTokenFinanceCellModel()

        this += sectionHeader
        this += selectedToken
    }

    private fun userTokensGroup(
        userTokensModels: List<SwapTokenModel.UserToken>
    ): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_all_tokens)
        val userTokens = userTokensModels
            .sortedWith(userTokensSorter)
            .map { it.toTokenFinanceCellModel() }

        this += sectionHeader
        this += userTokens
    }

    private fun allOtherTokensGroup(
        otherTokenModels: List<SwapTokenModel.JupiterToken>
    ): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_all_tokens)
        val otherTokens = otherTokenModels
            .sortedWith(otherTokensSorter)
            .map { it.toTokenFinanceCellModel() }

        this += sectionHeader
        this += otherTokens
    }
}
