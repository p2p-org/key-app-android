package org.p2p.wallet.swap.ui.jupiter.tokens.presenter

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.R
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel

class SearchSwapTokensMapper(
    private val commonMapper: SwapTokensCommonMapper
) {

    fun toCellItems(foundSwapTokens: List<SwapTokenModel>): List<AnyCellItem> {
        return commonMapper.foundTokensGroup(foundSwapTokens)
    }

    private fun SwapTokensCommonMapper.foundTokensGroup(
        foundSwapTokens: List<SwapTokenModel>
    ): List<AnyCellItem> = buildList {
        val sectionHeader = createSectionHeader(R.string.swap_tokens_section_search_result)
        val searchResultTokens = foundSwapTokens.map { it.toTokenFinanceCellModel() }

        this += sectionHeader
        this += searchResultTokens
    }
}
