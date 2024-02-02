package org.p2p.wallet.jupiter.ui.tokens.presenter

import org.p2p.core.utils.Constants
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel

class SwapTokensBMapper(
    private val commonMapper: SwapTokensCommonMapper,
) {
    suspend fun toTokenBCellItems(
        selectedTokenModel: SwapTokenModel,
        tokens: List<SwapTokenModel>
    ): List<AnyCellItem> = buildList {
        this += chosenTokenGroup(selectedTokenModel)
        this += popularTokensGroup(selectedTokenModel, tokens)
    }

    private suspend fun popularTokensGroup(
        selectedTokenModel: SwapTokenModel,
        allTokens: List<SwapTokenModel>
    ): List<AnyCellItem> = buildList {
        with(commonMapper) {
            val usdcToken = allTokens.find { it.mintAddress.base58Value == Constants.USDC_MINT }
            val usdtToken = allTokens.find { it.mintAddress.base58Value == Constants.USDT_MINT }
            val solToken = allTokens.find { it.mintAddress.base58Value == Constants.WRAPPED_SOL_MINT }
            val btcToken = allTokens.find { it.mintAddress.base58Value == Constants.WRAPPED_BTC_MINT }
            val ethToken = allTokens.find { it.mintAddress.base58Value == Constants.WRAPPED_ETH_MINT }

            val popularTokens = listOfNotNull(usdcToken, usdtToken, solToken, btcToken, ethToken)
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

    private suspend fun chosenTokenGroup(
        selectedTokenModel: SwapTokenModel
    ): List<AnyCellItem> = buildList {
        val sectionHeader = commonMapper.createSectionHeader(R.string.swap_tokens_section_chosen_token)
        val selectedToken = commonMapper.run { selectedTokenModel.toTokenFinanceCellModel() }

        this += sectionHeader
        this += selectedToken
    }
}
