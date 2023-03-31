package org.p2p.wallet.auth.model

data class Username(
    val value: String,
    val domainPrefix: String,
    // FIXME: New version of name service returns domain as well. We should remove domainPrefix asap
    val fullUsername: String = value + domainPrefix
)
