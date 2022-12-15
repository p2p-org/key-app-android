package org.p2p.wallet.infrastructure.network.environment

data class MoonpayEnvironment(
    val baseServerSideUrl: String,
    val baseClientSideUrl: String,
    val isSandboxEnabled: Boolean,
    val moonpayApiKey: String
) {
    val buyWidgetUrl: String = baseServerSideUrl + "buy/"
    val sellWidgetUrl: String = baseServerSideUrl + "sell/"
}
