package org.p2p.wallet.auth.model

data class Username(
    val trimmedUsername: String,
    val domainPrefix: String
) {
    val fullUsername: String = trimmedUsername + domainPrefix
}
