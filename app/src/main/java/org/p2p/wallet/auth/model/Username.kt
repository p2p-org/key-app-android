package org.p2p.wallet.auth.model

data class Username(
    val value: String,
    val domainPrefix: String,
    val fullUsername: String = value + domainPrefix
)
