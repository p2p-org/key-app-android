package org.p2p.wallet.home.ui.select

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token

private const val QUERY_MIN_LENGTH = 2

class SelectTokenPresenter(
    private val tokens: List<Token>
) : BasePresenter<SelectTokenContract.View>(), SelectTokenContract.Presenter {

    override fun load() = showTokens()

    override fun search(searchText: String) {
        if (searchText.length < QUERY_MIN_LENGTH) {
            view?.setNoSearchedTokensViewVisibility(false)
            showTokens()
            return
        }

        val filteredItems = tokens.filter {
            it.tokenName.startsWith(searchText, ignoreCase = true) || it.tokenSymbol.startsWith(searchText)
        }
        view?.apply {
            showTokens(filteredItems)
            setNoSearchedTokensViewVisibility(filteredItems.isEmpty())
        }
    }

    private fun showTokens() {
        view?.apply {
            showTokens(tokens)
            setEmptyViewVisibility(tokens.isEmpty())
        }
    }
}
