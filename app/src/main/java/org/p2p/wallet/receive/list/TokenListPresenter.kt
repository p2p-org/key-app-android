package org.p2p.wallet.receive.list

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.user.interactor.UserInteractor

private const val PAGE_SIZE = 20

class TokenListPresenter(
    private val interactor: UserInteractor
) : BasePresenter<TokenListContract.View>(), TokenListContract.Presenter {

    private var searchText = ""
    private var scrollToUp = false

    override fun attach(view: TokenListContract.View) {
        super.attach(view)
        observeTokens()
    }

    override fun load(isRefresh: Boolean, scrollToUp: Boolean) {
        launch {
            view?.showLoading(true)
            this@TokenListPresenter.scrollToUp = scrollToUp
            interactor.fetchTokens(searchText, PAGE_SIZE, isRefresh)
            view?.showLoading(false)
        }
    }

    override fun search(text: CharSequence?) {
        searchText = text.toString()
        if (searchText.isEmpty()) {
            view?.reset()
        }
        load(isRefresh = true, scrollToUp = true)
    }

    private fun observeTokens() {
        launch {
            interactor.getTokenListFlow().collect { tokens ->
                view?.showItems(tokens, scrollToUp)
            }
        }
    }
}