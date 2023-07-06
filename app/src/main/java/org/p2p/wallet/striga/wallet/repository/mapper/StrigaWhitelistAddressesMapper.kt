package org.p2p.wallet.striga.wallet.repository.mapper

import org.p2p.wallet.striga.wallet.api.response.StrigaWhitelistedAddressItemResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaWhitelistedAddressesResponse
import org.p2p.wallet.striga.wallet.models.StrigaBlockchainNetworkInfo
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId

class StrigaWhitelistAddressesMapper {
    fun fromNetwork(item: StrigaWhitelistedAddressItemResponse): StrigaWhitelistedAddressItem {
        return StrigaWhitelistedAddressItem(
            id = StrigaWhitelistedAddressId(item.id),
            status = StrigaWhitelistedAddressItem.Status.valueOf(item.status),
            address = item.address,
            currency = StrigaNetworkCurrency.valueOf(item.currency),
            network = item.network.run {
                StrigaBlockchainNetworkInfo(
                    name = name,
                    contractAddress = contractAddress,
                    type = type,
                )
            },
        )
    }

    fun fromNetwork(response: StrigaWhitelistedAddressesResponse): List<StrigaWhitelistedAddressItem> {
        return response.addresses.map(this::fromNetwork)
    }
}
