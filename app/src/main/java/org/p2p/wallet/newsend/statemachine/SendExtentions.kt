package org.p2p.wallet.newsend.statemachine

import kotlinx.coroutines.flow.StateFlow
import org.p2p.wallet.newsend.statemachine.model.SendFee
import org.p2p.wallet.newsend.statemachine.model.SendToken

val StateFlow<SendState>.lastStaticState: SendState.Static
    get() = value.lastStaticState

val SendState.lastStaticState: SendState.Static
    get() {
        return when (this) {
            is SendState.Loading -> lastStaticState
            is SendState.Exception -> lastStaticState
            is SendState.Static -> this
        }
    }

val SendState.Static.commonToken: SendToken.Common?
    get() = token as? SendToken.Common

val SendState.Static.token: SendToken?
    get() = when (this) {
        SendState.Static.Empty -> null
        is SendState.Static.ReadyToSend -> token
        is SendState.Static.TokenNotZero -> token
        is SendState.Static.TokenZero -> token
    }

val SendState.Static.commonFee: SendFee.Common?
    get() = fee as? SendFee.Common

val SendState.Static.fee: SendFee?
    get() = when (this) {
        SendState.Static.Empty -> null
        is SendState.Static.ReadyToSend -> fee
        is SendState.Static.TokenNotZero -> fee
        is SendState.Static.TokenZero -> fee
    }
