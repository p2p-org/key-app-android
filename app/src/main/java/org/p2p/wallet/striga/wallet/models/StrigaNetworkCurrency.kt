package org.p2p.wallet.striga.wallet.models

/**
 * List of supported currencies by striga
 */
enum class StrigaNetworkCurrency(val network: StrigaNetworkType) {
    BTC(StrigaNetworkType.BTC),
    USDT(StrigaNetworkType.ETH),
    USDC(StrigaNetworkType.ETH),
    ETH(StrigaNetworkType.ETH),
    BUSD(StrigaNetworkType.BSC),
    BNB(StrigaNetworkType.BSC),

    // TODO: is not ready yet, idk what it would be called
    // USDC_SOL(StrigaNetworkType.SOL),
}
