package org.p2p.wallet.receive.tokenselect

import kotlinx.coroutines.launch
import org.p2p.core.utils.Constants
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.emptyString

private const val PAGE_SIZE = 20

class ReceiveTokensPresenter(
    private val interactor: UserInteractor,
) : BasePresenter<ReceiveTokensContract.View>(), ReceiveTokensContract.Presenter {

    private var searchText = emptyString()
    private var scrollToUp = false

    override fun attach(view: ReceiveTokensContract.View) {
        super.attach(view)
        observeTokens()
        launch {
            val tokensForReceiveBanner = interactor.getTokensForBuy(
                availableTokensSymbols = listOf(
                    Constants.SOL_SYMBOL,
                    Constants.ETH_SYMBOL
                )
            )
            view.setBannerTokens(
                tokensForReceiveBanner[0].iconUrl.orEmpty(),
                tokensForReceiveBanner[1].iconUrl.orEmpty()
            )
        }
    }

    override fun load(isRefresh: Boolean, scrollToUp: Boolean) {
        launch {
            this@ReceiveTokensPresenter.scrollToUp = scrollToUp
            interactor.fetchTokens(searchText, PAGE_SIZE, isRefresh)
        }
    }

    override fun onSearchTokenQueryChanged(newQuery: String) {
        searchText = newQuery
        if (newQuery.isBlank()) {
            view?.reset()
        }
        load(isRefresh = true, scrollToUp = true)
    }

    private fun observeTokens() {
        launch {
            interactor.getTokenListFlow().collect { data ->
                val isEmpty = data.result.isEmpty()
                view?.showEmptyState(isEmpty)
                view?.setBannerVisibility(!isEmpty && searchText.isEmpty())
                view?.showTokenItems(data.result, scrollToUp)
            }
        }
    }
}
