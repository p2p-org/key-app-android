package org.p2p.wallet.home.ui.select

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token

private const val QUERY_MIN_LENGTH = 2

class SelectTokenPresenter(
    private val allTokens: List<Token>
) : BasePresenter<SelectTokenContract.View>(), SelectTokenContract.Presenter {

    override fun load() {
        showTokens()
    }

    override fun search(searchText: String) {
        if (searchText.length < QUERY_MIN_LENGTH) {
            view?.setTokenNotFoundViewVisibility(isVisible = false)
            showTokens()
            return
        }

        val filteredItems = allTokens.filter {
            it.tokenName.contains(searchText, ignoreCase = true) ||
                it.tokenSymbol.contains(searchText, ignoreCase = true)
        }
        view?.apply {
            showTokens(filteredItems)
            setTokenNotFoundViewVisibility(filteredItems.isEmpty())
        }
    }

    private fun showTokens() {
        view?.apply {
            showTokens(allTokens)
            setEmptyViewVisibility(isVisible = allTokens.isEmpty())
        }
    }
}
