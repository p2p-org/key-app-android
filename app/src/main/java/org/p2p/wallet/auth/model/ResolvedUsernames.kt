package org.p2p.wallet.auth.model

import org.p2p.wallet.utils.Base58String

data class ResolvedUsernames(
    val owner: Base58String,
    val username: String,
)
