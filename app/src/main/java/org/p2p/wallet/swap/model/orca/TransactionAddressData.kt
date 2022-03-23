package org.p2p.wallet.swap.model.orca

import org.p2p.solanaj.core.PublicKey

class TransactionAddressData(
    val destinationAddress: PublicKey,
    val shouldCreateAccount: Boolean
)
