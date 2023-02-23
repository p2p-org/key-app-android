package org.p2p.wallet.swap.ui.jupiter.tokens.presenter

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensContract
import org.p2p.wallet.swap.ui.jupiter.tokens.interactor.SwapTokensInteractor

class SwapTokensPresenter(
    private val tokenToChange: SwapTokensListMode,
    private val mapper: SwapTokensToCellItemsMapper,
    private val interactor: SwapTokensInteractor,
) : BasePresenter<SwapTokensContract.View>(), SwapTokensContract.Presenter {

    private var allTokens: List<SwapTokenModel> = emptyList()
    private var currentToken: SwapTokenModel? = null

    override fun attach(view: SwapTokensContract.View) {
        super.attach(view)

        launch {
            val currentTokenToSwapTokens = when (tokenToChange) {
                SwapTokensListMode.TOKEN_A -> interactor.getCurrentTokenA() to interactor.getAllTokensA()
                SwapTokensListMode.TOKEN_B -> interactor.getCurrentTokenB() to interactor.getAllAvailableTokensB()
            }
            currentToken = currentTokenToSwapTokens.first
            allTokens = currentTokenToSwapTokens.second
            renderAllSwapTokensList(allTokens)
        }
    }

    private fun renderAllSwapTokensList(tokens: List<SwapTokenModel>) {
        val cellItems = mapper.toCellItems(
            chosenToken = currentToken ?: return,
            swapTokens = tokens
        )
        view?.setTokenItems(cellItems)
    }

    override fun onSearchTokenQueryChanged(newQuery: String) {
        if (newQuery.isBlank()) {
            renderAllSwapTokensList(allTokens)
        } else {
            launch {
                renderSearchTokenList(interactor.searchToken(tokenToChange, newQuery))
            }
        }
    }

    private fun renderSearchTokenList(searchResult: List<SwapTokenModel>) {
        val cellItems = mapper.toSearchResultCellModels(searchResult)
        view?.setTokenItems(cellItems)
    }

    override fun onTokenClicked(clickedToken: SwapTokenModel) {
        view?.showUiKitSnackBar(clickedToken.tokenName)
    }
}
