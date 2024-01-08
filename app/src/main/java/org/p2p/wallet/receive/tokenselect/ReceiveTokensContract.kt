package org.p2p.wallet.receive.tokenselect

import org.p2p.core.token.TokenMetadata
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.receive.tokenselect.models.ReceiveNetwork
import org.p2p.wallet.receive.tokenselect.models.ReceiveTokenPayload

interface ReceiveTokensContract {
    interface View : MvpView {
        fun setBannerTokens(firstTokenUrl: String, secondTokenUrl: String)
        fun showTokenItems(items: List<AnyCellItem>)
        fun showEmptyState(isEmpty: Boolean)
        fun setBannerVisibility(isVisible: Boolean)
        fun resetView()
        fun showSelectNetworkDialog()
        fun openReceiveInSolana(tokenMetadata: TokenMetadata)
        fun openReceiveInEthereum(tokenMetadata: TokenMetadata)
    }

    interface Presenter : MvpPresenter<View> {
        fun load(isRefresh: Boolean, scrollToUp: Boolean = false)
        fun onSearchTokenQueryChanged(newQuery: String)
        fun onTokenClicked(tokenDataPayload: ReceiveTokenPayload)
        fun onNetworkSelected(network: ReceiveNetwork)
    }
}
