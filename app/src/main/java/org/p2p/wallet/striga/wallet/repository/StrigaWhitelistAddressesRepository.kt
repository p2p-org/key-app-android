package org.p2p.wallet.striga.wallet.repository

import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem

interface StrigaWhitelistAddressesRepository {

    /**
     * Calling this method twice for the same address will trigger an error:
     * [org.p2p.wallet.striga.model.StrigaApiErrorCode.ADDRESS_ALREADY_WHITELISTED] - address already exists
     *
     * @param address The address to whitelist. It must correspond to the [currency]
     * @param currency One of the supported currency
     * @param label An optional label to tag your address. Must be unique. A string up to 30 characters.
     */
    suspend fun whitelistAddress(
        address: String,
        currency: StrigaNetworkCurrency,
        addressLabel: String? = null
    ): StrigaDataLayerResult<StrigaWhitelistedAddressItem>

    /**
     * Get all whitelisted users' addresses
     *
     * @param label An optional label to filter by
     */
    suspend fun getWhitelistedAddresses(
        addressLabel: String? = null
    ): StrigaDataLayerResult<List<StrigaWhitelistedAddressItem>>
}
