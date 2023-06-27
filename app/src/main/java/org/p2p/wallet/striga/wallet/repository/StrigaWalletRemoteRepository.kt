package org.p2p.wallet.striga.wallet.repository

import java.math.BigInteger
import java.util.Calendar
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.request.StrigaAddWhitelistedAddressRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaEnrichAccountRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaGetWhitelistedAddressesRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaInitiateOnchainWithdrawalRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaUserWalletsRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaInitWithdrawalRequest
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId

class StrigaWalletRemoteRepository(
    private val api: StrigaWalletApi,
    private val mapper: StrigaWalletRepositoryMapper,
    private val walletsMapper: StrigaUserWalletsMapper,
    private val strigaUserIdProvider: StrigaUserIdProvider
) : StrigaWalletRepository {

    override suspend fun initiateOnchainWithdrawal(
        sourceAccountId: StrigaAccountId,
        whitelistedAddressId: StrigaWhitelistedAddressId,
        amount: BigInteger,
    ): StrigaDataLayerResult<StrigaInitWithdrawalDetails> {
        return try {
            val request = StrigaInitWithdrawalRequest(
                userId = userIdProvider.getUserIdOrThrow(),
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

    override suspend fun whitelistAddress(
        address: String,
        currency: StrigaNetworkCurrency,
        label: String?
    ): StrigaDataLayerResult<StrigaWhitelistedAddressItem> {
        return try {
            val request = StrigaAddWhitelistedAddressRequest(
                userId = userIdProvider.getUserIdOrThrow(),
                addressToWhitelist = address,
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
        label: String?
    ): StrigaDataLayerResult<List<StrigaWhitelistedAddressItem>> {
        return try {
            val request = StrigaGetWhitelistedAddressesRequest(
                userId = userIdProvider.getUserIdOrThrow(),
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
        accountId: StrigaAccountId
    ): StrigaDataLayerResult<StrigaFiatAccountDetails> {
        return try {
            val request = StrigaEnrichAccountRequest(
                userId = userIdProvider.getUserIdOrThrow(),
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

    override suspend fun getUserWallet(): StrigaDataLayerResult<StrigaUserWallet> {
        return try {
            val hardcodedStartDate = Calendar.getInstance().apply { set(2023, 6, 26) }.timeInMillis
            val request = StrigaUserWalletsRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                startDate = hardcodedStartDate,
                endDate = System.currentTimeMillis(),
                page = 1
            )
            val response = api.getUserWallets(request)

            walletsMapper.fromNetwork(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                response = response
            ).toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }
}
