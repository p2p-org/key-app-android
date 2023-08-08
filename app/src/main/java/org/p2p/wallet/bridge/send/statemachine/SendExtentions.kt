package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal
import kotlinx.coroutines.flow.StateFlow
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

val StateFlow<BridgeSendState>.lastStaticState: BridgeSendState.Static
    get() = value.lastStaticState

val BridgeSendState.lastStaticState: BridgeSendState.Static
    get() {
        return when (this) {
            is BridgeSendState.Loading -> lastStaticState
            is BridgeSendState.Exception -> lastStaticState
            is BridgeSendState.Static -> this
        }
    }

val BridgeSendState.Static.bridgeToken: SendToken.Bridge?
    get() = token as? SendToken.Bridge

val BridgeSendState.Static.token: SendToken?
    get() = when (this) {
        BridgeSendState.Static.Empty -> null
        is BridgeSendState.Static.ReadyToSend -> token
        is BridgeSendState.Static.TokenNotZero -> token
        is BridgeSendState.Static.TokenZero -> token
    }

val BridgeSendState.Static.bridgeFee: SendFee.Bridge?
    get() = fee as? SendFee.Bridge

val BridgeSendState.Static.fee: SendFee?
    get() = when (this) {
        BridgeSendState.Static.Empty -> null
        is BridgeSendState.Static.ReadyToSend -> fee
        is BridgeSendState.Static.TokenNotZero -> fee
        is BridgeSendState.Static.TokenZero -> fee
    }

val BridgeSendState.Static.inputAmount: BigDecimal?
    get() = when (this) {
        BridgeSendState.Static.Empty -> null
        is BridgeSendState.Static.ReadyToSend -> amount
        is BridgeSendState.Static.TokenNotZero -> amount
        is BridgeSendState.Static.TokenZero -> null
    }
