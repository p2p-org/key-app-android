package org.p2p.wallet.auth.username.repository.model

import org.p2p.wallet.auth.model.Username
import org.p2p.core.crypto.Base58String

data class UsernameDetails(
    val ownerAddress: Base58String,
    val username: Username,
)
