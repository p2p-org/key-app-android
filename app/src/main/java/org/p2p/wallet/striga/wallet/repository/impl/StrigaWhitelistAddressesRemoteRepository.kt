package org.p2p.wallet.striga.wallet.repository.impl

import timber.log.Timber
import org.p2p.wallet.striga.common.StrigaUserIdProvider
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.common.model.toSuccessResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.request.StrigaAddWhitelistedAddressRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaGetWhitelistedAddressesRequest
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWhitelistAddressesMapper
import org.p2p.wallet.striga.wallet.repository.StrigaWhitelistAddressesRepository

private const val TAG = "StrigaWhitelistAddressesRemoteRepository"

class StrigaWhitelistAddressesRemoteRepository(
    private val api: StrigaWalletApi,
    private val mapper: StrigaWhitelistAddressesMapper,
    private val strigaUserIdProvider: StrigaUserIdProvider,
) : StrigaWhitelistAddressesRepository {
    private val timber: Timber.Tree = Timber.tag(TAG)

    override suspend fun whitelistAddress(
        address: String,
        currency: StrigaNetworkCurrency,
        addressLabel: String?
    ): StrigaDataLayerResult<StrigaWhitelistedAddressItem> = try {
        timber.i("whitelistAddress started")
        val request = StrigaAddWhitelistedAddressRequest(
            userId = strigaUserIdProvider.getUserIdOrThrow(),
            addressToWhitelist = address,
            currency = currency.name,
            network = currency.network.name,
            label = addressLabel
        )
        val response = api.addWhitelistedAddress(request)
        mapper.fromNetwork(response).toSuccessResult()
    } catch (error: Throwable) {
        timber.i(error, "whitelistAddress failed")
        StrigaDataLayerError.from(
            error = error,
            default = StrigaDataLayerError.InternalError(error)
        )
    }

    override suspend fun getWhitelistedAddresses(
        addressLabel: String?
    ): StrigaDataLayerResult<List<StrigaWhitelistedAddressItem>> = try {
        timber.i("getWhitelistedAddresses started")
        val request = StrigaGetWhitelistedAddressesRequest(
            userId = strigaUserIdProvider.getUserIdOrThrow(),
            label = addressLabel
        )
        val response = api.getWhitelistedAddresses(request)
        mapper.fromNetwork(response).toSuccessResult()
    } catch (error: Throwable) {
        timber.i(error, "getWhitelistedAddresses failed")
        StrigaDataLayerError.from(
            error = error,
            default = StrigaDataLayerError.InternalError(error)
        )
    }
}
