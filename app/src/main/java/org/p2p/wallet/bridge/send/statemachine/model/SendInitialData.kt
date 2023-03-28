package org.p2p.wallet.bridge.send.statemachine.model

import java.math.BigDecimal
import org.p2p.core.wrapper.eth.EthAddress

sealed interface SendInitialData {

    data class Bridge(
        val initialToken: SendToken.Bridge,
        val initialAmount: BigDecimal?,
        val recipient: EthAddress,
    )
}
