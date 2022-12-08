package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.PublicKey
import org.p2p.core.utils.orZero
import java.math.BigInteger

class RelayAccount(
    val publicKey: PublicKey,
    val isCreated: Boolean,
    val balance: BigInteger?
) {

    /**
     * Relay account should always have a minimum required balance in amount of [relayRentExemption]
     * Now it equals to 890880 lamports
     * We can spend any lamports until this amount
     * */
    fun getMinRemainingBalance(relayRentExemption: BigInteger): BigInteger =
        balance.orZero() - relayRentExemption
}
