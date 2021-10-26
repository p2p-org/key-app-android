package org.p2p.wallet.swap.model.orca

import org.p2p.solanaj.core.PublicKey

data class OrcaAddressData(
    val associatedAddress: PublicKey,
    val shouldCreateAssociatedInstruction: Boolean
)