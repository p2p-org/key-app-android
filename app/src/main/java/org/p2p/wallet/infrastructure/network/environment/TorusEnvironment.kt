package org.p2p.wallet.infrastructure.network.environment

data class TorusEnvironment(
    val baseUrl: String,
    val verifier: String,
    val subVerifier: String?,
    val torusNetwork: String,
    val torusLogLevel: String
)
