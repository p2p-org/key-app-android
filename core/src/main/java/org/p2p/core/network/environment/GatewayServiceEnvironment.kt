package org.p2p.core.network.environment

import java.net.URI

data class GatewayServiceEnvironment(
    val baseUrl: String,
    val isProdSelected: Boolean
) {
    val baseUrlUri: URI
        get() = URI(baseUrl)
}
