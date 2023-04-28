package org.p2p.wallet.receive.list

import kotlinx.coroutines.launch
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.emptyString

private const val PAGE_SIZE = 20

class TokenListPresenter(
    private val interactor: UserInteractor,
    private val browseAnalytics: BrowseAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor
) : BasePresenter<TokenListContract.View>(), TokenListContract.Presenter {

    private var searchText = emptyString()
    private var scrollToUp = false

    override fun attach(view: TokenListContract.View) {
        super.attach(view)
        browseAnalytics.logTokenListViewed(
            analyticsInteractor.getPreviousScreenName(),
            BrowseAnalytics.TokenListLocation.RECEIVE
        )
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
        } else {
            browseAnalytics.logTokenListSearching(text.toString())
        }
        load(isRefresh = true, scrollToUp = true)
    }

    private fun observeTokens() {
        launch {
            interactor.getTokenListFlow().collect { data ->
                if (data.result.isNotEmpty()) {
                    view?.showItems(data.result, scrollToUp)
                } else {
                    view?.showEmpty(data.searchText)
                }
            }
        }
    }
}
