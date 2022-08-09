package org.p2p.wallet.home.ui.main.adapter

import org.p2p.wallet.home.model.Token

interface OnHomeItemsClickListener {
    fun onBannerClicked(bannerId: Int)
    fun onTokenClicked(token: Token.Active)
    fun onPopularTokenClicked(token: Token)
    fun onToggleClicked()
    fun onHideClicked(token: Token.Active)
}
