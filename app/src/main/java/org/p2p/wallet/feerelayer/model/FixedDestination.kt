package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.PublicKey
@Deprecated("Old swap")
class FixedDestination(
    val destinationToken: TokenAccount,
    val userDestinationAccountOwnerAddress: PublicKey?,
    val needsCreateDestinationTokenAccount: Boolean
)
