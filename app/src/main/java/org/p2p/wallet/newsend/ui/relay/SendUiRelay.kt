package org.p2p.wallet.newsend.ui.relay

import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.ui.NewSendContract

interface SendUiRelay {

    fun handleFeatureState(state: SendState, view: NewSendContract.View?)
}
