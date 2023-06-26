package org.p2p.wallet.striga.wallet.repository

import java.math.BigInteger
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.request.StrigaAddWhitelistedAddressRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaEnrichAccountRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaGetWhitelistedAddressesRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaInitiateOnchainWithdrawalRequest
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaInitiateOnchainWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId

class StrigaWalletRemoteRepository(
    private val api: StrigaWalletApi,
    private val mapper: StrigaWalletRepositoryMapper,
) : StrigaWalletRepository {

    override suspend fun initiateOnchainWithdrawal(
        userId: String,
        sourceAccountId: StrigaAccountId,
        whitelistedAddressId: StrigaWhitelistedAddressId,
        amount: BigInteger,
    ): StrigaDataLayerResult<StrigaInitiateOnchainWithdrawalDetails> {
        return try {
            val request = StrigaInitiateOnchainWithdrawalRequest(
                userId = userId,
                sourceAccountId = sourceAccountId.value,
                whitelistedAddressId = whitelistedAddressId.value,
                amount = amount.toString()
            )
            val response = api.initiateOnchainWithdrawal(request)
            return mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }

    override suspend fun addWhitelistedAddress(
        userId: String,
        address: String,
        currency: StrigaNetworkCurrency,
        label: String?
    ): StrigaDataLayerResult<StrigaWhitelistedAddressItem> {
        return try {
            val request = StrigaAddWhitelistedAddressRequest(
                userId = userId,
                address = address,
                currency = currency.name,
                network = currency.network.name,
                label = label
            )
            val response = api.addWhitelistedAddress(request)
            return mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }

    override suspend fun getWhitelistedAddresses(
        userId: String,
        label: String?
    ): StrigaDataLayerResult<List<StrigaWhitelistedAddressItem>> {
        return try {
            val request = StrigaGetWhitelistedAddressesRequest(
                userId = userId,
                label = null
            )
            val response = api.getWhitelistedAddresses(request)
            return mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }

    override suspend fun getFiatAccountDetails(
        userId: String,
        accountId: StrigaAccountId
    ): StrigaDataLayerResult<StrigaFiatAccountDetails> {
        return try {
            val request = StrigaEnrichAccountRequest(
                userId = userId,
                accountId = accountId.value
            )
            val response = api.enrichFiatAccount(request)
            return mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }
}
