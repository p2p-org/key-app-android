package org.p2p.wallet.swap.ui.jupiter.tokens.presenter

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensContract
import org.p2p.wallet.swap.ui.jupiter.tokens.interactor.SwapTokensInteractor

class SwapTokensPresenter(
    private val tokenToChange: SwapTokensChangeToken,
    private val mapper: SwapTokensToCellItemsMapper,
    private val interactor: SwapTokensInteractor,
) : BasePresenter<SwapTokensContract.View>(), SwapTokensContract.Presenter {

    override fun attach(view: SwapTokensContract.View) {
        super.attach(view)

        launch {
            val currentTokenToSwapTokens = when (tokenToChange) {
                SwapTokensChangeToken.TOKEN_A -> interactor.getCurrentTokenA() to interactor.getAllTokensA()
                else -> return@launch
            }
            val cellItems = mapper.toCellItems(
                chosenToken = currentTokenToSwapTokens.first,
                swapTokens = currentTokenToSwapTokens.second
            )
            view.setTokenItems(cellItems)
        }
    }

    override fun onSearchTokenQueryChanged(newQuery: String) {
    }

    override fun onTokenClicked(clickedToken: SwapTokenModel) {
        view?.showUiKitSnackBar(clickedToken.tokenName)
    }
}
