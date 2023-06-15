package org.p2p.wallet.home.ui.main.adapter

import org.p2p.core.token.Token

interface OnHomeItemsClickListener {
    fun onBannerClicked(bannerTitleId: Int)
    fun onTokenClicked(token: Token.Active)
    fun onPopularTokenClicked(token: Token)
    fun onToggleClicked()
    fun onHideClicked(token: Token.Active)
    fun onClaimTokenClicked(canBeClaimed: Boolean, token: Token.Eth)
    fun onBannerCloseClicked(bannerTitleId: Int)
}
