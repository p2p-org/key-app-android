package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.PublicKey

class FixedDestination(
    val destinationToken: TokenInfo,
    val userDestinationAccountOwnerAddress: PublicKey?,
    val needsCreateDestinationTokenAccount: Boolean
)
