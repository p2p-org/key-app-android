package org.p2p.wallet.jupiter.ui.tokens.presenter

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel

class SearchSwapTokensMapper(
    private val commonMapper: SwapTokensCommonMapper,
) {

    suspend fun toCellItems(
        foundSwapTokens: List<SwapTokenModel>
    ): List<AnyCellItem> = commonMapper.foundTokensGroup(foundSwapTokens)

    private suspend fun SwapTokensCommonMapper.foundTokensGroup(
        foundSwapTokens: List<SwapTokenModel>
    ): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_search_result)
        val searchResultTokens = foundSwapTokens.map { it.toTokenFinanceCellModel(isSearchResult = true) }

        this += sectionHeader
        this += searchResultTokens
    }
}
