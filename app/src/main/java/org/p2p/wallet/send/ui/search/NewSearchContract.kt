package org.p2p.wallet.send.ui.search

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.core.token.Token
import org.p2p.wallet.send.model.SearchResult

interface NewSearchContract {

    interface View : MvpView {
        fun showSearchValue(value: String)
        fun showSearchResult(result: List<SearchResult>)
        fun showMessage(@StringRes textRes: Int?)
        fun showLoading(isLoading: Boolean)
        fun showNotFound()
        fun showEmptyState(isEmpty: Boolean)
        fun showErrorState()
        fun setListBackgroundVisibility(isVisible: Boolean)
        fun setContinueButtonVisibility(isVisible: Boolean)
        fun setBuyReceiveButtonsVisibility(isVisible: Boolean)
        fun submitSearchResult(searchResult: SearchResult)
        fun showScanner()
        fun showBuyScreen(token: Token)
    }

    interface Presenter : MvpPresenter<View> {
        fun search(newQuery: String)
        fun loadInitialData()
        fun onSearchResultClick(result: SearchResult)
        fun onScanClicked()
        fun onContinueClicked()
        fun onBuyClicked()
    }
}
