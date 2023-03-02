package org.p2p.wallet.swap.ui.jupiter.settings.presenter

enum class SwapSlippagePayload {
    ZERO_POINT_ONE, ZERO_POINT_FIVE, ONE, CUSTOM
}

enum class SwapSettingsPayload {
    ROUTE, NETWORK_FEE, CREATION_FEE, LIQUIDITY_FEE, ESTIMATED_FEE, MINIMUM_RECEIVED
}
