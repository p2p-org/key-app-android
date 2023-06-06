package org.p2p.wallet.newsend.ui.search

import androidx.annotation.StringRes
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.svl.model.SvlWidgetState

interface NewSearchContract {

    interface View : MvpView {
        fun updateSearchInput(recentQuery: String, submit: Boolean)
        fun showUsers(result: List<SearchResult>)
        fun showSendViaLink(isVisible: Boolean)
        fun updateLinkWidgetState(state: SvlWidgetState)
        fun clearUsers()
        fun showUsersMessage(@StringRes textRes: Int?)
        fun showLoading(isLoading: Boolean)
        fun showNotFound()
        fun showEmptyState(isEmpty: Boolean)
        fun showErrorState()
        fun showBackgroundVisible(isVisible: Boolean)
        fun submitSearchResult(searchResult: SearchResult, initialToken: Token.Active?)
        fun showScanner()
    }

    interface Presenter : MvpPresenter<View> {
        fun search(newQuery: String)
        fun onSearchResultClick(result: SearchResult)
        fun onScanClicked()
    }
}
