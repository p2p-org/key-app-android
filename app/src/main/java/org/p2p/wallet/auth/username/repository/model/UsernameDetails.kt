package org.p2p.wallet.auth.username.repository.model

import org.p2p.wallet.utils.Base58String

data class UsernameDetails(
    val ownerAddress: Base58String,
    val fullUsername: String,
)
