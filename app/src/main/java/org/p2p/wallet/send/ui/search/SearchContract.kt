package org.p2p.wallet.send.ui.search

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.Target

interface SearchContract {

    interface View : MvpView {
        fun showSearchValue(value: String)
        fun showResult(result: List<SearchResult>)
        fun showMessage(@StringRes textRes: Int?)
        fun showLoading(isLoading: Boolean)
        fun submitSearchResult(searchResult: SearchResult)
    }

    interface Presenter : MvpPresenter<View> {
        fun search(target: Target)
        fun loadInitialData()
        fun onSearchResultClick(result: SearchResult)
    }
}
