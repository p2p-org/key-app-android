package org.p2p.wallet.auth.model

/**
 * @see [org.p2p.wallet.auth.username.repository.UsernameParser]
 */
data class Username(
    val value: String,
    val domainPrefix: String,
) {
    val fullUsername: String = value + domainPrefix
}
