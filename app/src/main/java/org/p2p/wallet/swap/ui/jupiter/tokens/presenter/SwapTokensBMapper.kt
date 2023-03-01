package org.p2p.wallet.swap.ui.jupiter.tokens.presenter

import org.p2p.core.utils.Constants
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.R
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel

class SwapTokensBMapper(private val commonMapper: SwapTokensCommonMapper) {

    fun toTokenBCellItems(
        selectedTokenModel: SwapTokenModel,
        tokens: List<SwapTokenModel>
    ): List<AnyCellItem> = buildList {
        this += chosenTokenGroup(selectedTokenModel)
        this += popularTokensGroup(selectedTokenModel, tokens)
    }

    private fun popularTokensGroup(
        selectedTokenModel: SwapTokenModel,
        allTokens: List<SwapTokenModel>
    ): List<AnyCellItem> = buildList {
        with(commonMapper) {
            val usdcToken = allTokens.find { it.tokenSymbol.equals(Constants.USDC_SYMBOL, ignoreCase = true) }
            val usdtToken = allTokens.find { it.tokenSymbol.equals(Constants.USDT_SYMBOL, ignoreCase = true) }
            val solToken = allTokens.find { it.tokenSymbol.equals(Constants.SOL_SYMBOL, ignoreCase = true) }
            val ethToken = allTokens.find { it.tokenSymbol.equals(Constants.ETH_SYMBOL, ignoreCase = true) }

            val popularTokens = listOfNotNull(usdcToken, usdtToken, solToken, ethToken)
            // order matters
            val popularTokensModels = popularTokens
                .filterNot { it.isSelectedToken(selectedTokenModel) }
                .map { it.toTokenFinanceCellModel(isPopularToken = true) }

            val notPopularToken = { it: SwapTokenModel ->
                it !in popularTokens && !it.isSelectedToken(selectedTokenModel)
            }
            val otherTokens = allTokens
                .filter(notPopularToken)
                .sortedWith(otherTokensSorter)
                .map { it.toTokenFinanceCellModel() }

            val sectionHeader = commonMapper.createSectionHeader(R.string.swap_tokens_section_all_tokens)

            add(sectionHeader)
            addAll(popularTokensModels)
            addAll(otherTokens)
        }
    }

    private fun chosenTokenGroup(
        selectedTokenModel: SwapTokenModel
    ): List<AnyCellItem> = buildList {
        val sectionHeader = commonMapper.createSectionHeader(R.string.swap_tokens_section_chosen_token)
        val selectedToken = commonMapper.run { selectedTokenModel.toTokenFinanceCellModel() }

        this += sectionHeader
        this += selectedToken
    }
}
