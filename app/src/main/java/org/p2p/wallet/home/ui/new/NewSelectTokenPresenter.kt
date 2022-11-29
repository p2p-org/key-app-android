package org.p2p.wallet.home.ui.new

import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.SelectTokenItem
import org.p2p.wallet.home.model.SelectTokenItem.CategoryTitle
import org.p2p.wallet.home.model.SelectTokenItem.SelectableToken
import org.p2p.wallet.home.model.SelectableTokenRoundedState.BOTTOM_ROUNDED
import org.p2p.wallet.home.model.SelectableTokenRoundedState.NOT_ROUNDED
import org.p2p.wallet.home.model.SelectableTokenRoundedState.ROUNDED
import org.p2p.wallet.home.model.SelectableTokenRoundedState.TOP_ROUNDED
import org.p2p.wallet.home.model.Token

private const val QUERY_MIN_LENGTH = 2

class NewSelectTokenPresenter : BasePresenter<NewSelectTokenContract.View>(), NewSelectTokenContract.Presenter {

    private val mappedTokens = mutableListOf<SelectTokenItem>()

    override fun load(tokens: List<Token.Active>, selectedToken: Token.Active?) {
        mapInitialTokens(selectedToken, tokens)

        view?.showTokens(mappedTokens)
        view?.showEmptyState(isVisible = mappedTokens.isEmpty())
    }

    override fun search(lowerCasedSearchText: String) {
        // restoring the list text is too short
        if (lowerCasedSearchText.length < QUERY_MIN_LENGTH) {
            view?.showEmptyState(isVisible = false)
            view?.showTokens(mappedTokens)
            view?.showEmptyState(isVisible = mappedTokens.isEmpty())
            return
        }

        // start searching
        val filteredItems = mappedTokens
            .filter { item ->
                if (item !is SelectableToken) return@filter false

                val tokenName = item.token.tokenName.lowercase()
                val tokenSymbol = item.token.tokenSymbol.lowercase()

                tokenName.contains(lowerCasedSearchText) || tokenSymbol.contains(lowerCasedSearchText)
            }
            .toMutableList()

        @Suppress("UNCHECKED_CAST")
        val updatedRoundStates = updateRoundedStates(filteredItems as List<SelectableToken>)

        if (updatedRoundStates.isEmpty()) {
            view?.clearTokens()
            view?.showEmptyState(isVisible = true)
        } else {
            updatedRoundStates.add(0, CategoryTitle(R.string.send_pick_token_search_result_text))
            view?.showTokens(updatedRoundStates)
            view?.showEmptyState(isVisible = false)
        }
    }

    private fun mapInitialTokens(
        selectedToken: Token.Active?,
        tokens: List<Token.Active>
    ) {
        if (selectedToken != null) {
            mappedTokens += CategoryTitle(R.string.send_pick_token_chosen)
            mappedTokens += SelectableToken(selectedToken, ROUNDED)
        }

        val otherTokens = tokens.filterNot { it.publicKey == selectedToken?.publicKey }
        if (otherTokens.isNotEmpty()) {
            mappedTokens += CategoryTitle(R.string.send_pick_token_other)

            val otherTokensSize = otherTokens.size
            mappedTokens += otherTokens.mapIndexed { index, token ->
                if (otherTokensSize == 1) {
                    return@mapIndexed SelectableToken(token, ROUNDED)
                }

                val roundedState = when (index) {
                    0 -> TOP_ROUNDED
                    otherTokensSize - 1 -> BOTTOM_ROUNDED
                    else -> NOT_ROUNDED
                }

                return@mapIndexed SelectableToken(token, roundedState)
            }
        }
    }

    /*
    * Updating corners when the list is filtered according to the search query
    * In this case every viewHolder should update its corners radius or remove it if needed
    * */
    private fun updateRoundedStates(mappedTokens: List<SelectableToken>): MutableList<SelectTokenItem> {
        val mappedTokensSize = mappedTokens.size

        val updatedList = mutableListOf<SelectTokenItem>()

        mappedTokens.forEachIndexed { index, item ->
            if (mappedTokensSize == 1) {
                updatedList += SelectableToken(item.token, ROUNDED)
                return@forEachIndexed
            }

            val roundedState = when (index) {
                0 -> TOP_ROUNDED
                mappedTokensSize - 1 -> BOTTOM_ROUNDED
                else -> NOT_ROUNDED
            }

            updatedList += SelectableToken(item.token, roundedState)
            return@forEachIndexed
        }

        return updatedList
    }
}
