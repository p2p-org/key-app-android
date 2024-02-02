package org.p2p.wallet.jupiter.ui.tokens.presenter

import org.p2p.core.utils.orZero
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel

class SwapTokensAMapper(
    private val commonMapper: SwapTokensCommonMapper,
) {

    private val userTokensSorter: Comparator<SwapTokenModel.UserToken> =
        compareByDescending(commonMapper.byTokenMintComparator, SwapTokenModel.UserToken::mintAddress)
            .thenByDescending { it.tokenAmountInUsd.orZero() }

    suspend fun toTokenACellItems(
        chosenToken: SwapTokenModel,
        swapTokens: List<SwapTokenModel>
    ): List<AnyCellItem> = buildList {
        this += commonMapper.chosenTokenGroup(chosenToken)

        this += commonMapper.userTokensGroup(swapTokens.filterIsInstance<SwapTokenModel.UserToken>())
        this += commonMapper.allOtherTokensGroup(swapTokens.filterIsInstance<SwapTokenModel.JupiterToken>())
    }

    private suspend fun SwapTokensCommonMapper.chosenTokenGroup(
        selectedTokenModel: SwapTokenModel
    ): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_chosen_token)
        val selectedToken = selectedTokenModel.toTokenFinanceCellModel()

        this += sectionHeader
        this += selectedToken
    }

    private suspend fun SwapTokensCommonMapper.userTokensGroup(
        userTokensModels: List<SwapTokenModel.UserToken>
    ): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_all_tokens)
        val userTokens = userTokensModels
            .sortedWith(userTokensSorter)
            .map { it.toTokenFinanceCellModel() }

        this += sectionHeader
        this += userTokens
    }

    private suspend fun SwapTokensCommonMapper.allOtherTokensGroup(
        otherTokenModels: List<SwapTokenModel.JupiterToken>
    ): List<AnyCellItem> = buildList {
        val otherTokens = otherTokenModels
            .sortedWith(otherTokensSorter)
            .map { it.toTokenFinanceCellModel() }

        this += otherTokens
    }
}
