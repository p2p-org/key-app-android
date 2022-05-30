package org.p2p.wallet.send.model

import org.p2p.wallet.home.model.Token

sealed interface SelectionMechanismState {
    object HideFee : SelectionMechanismState
    class UpdateFeePayer(val feePayer: Token.Active) : SelectionMechanismState
}
