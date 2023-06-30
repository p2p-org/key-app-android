package org.p2p.wallet.striga.wallet.repository

import java.math.BigInteger
import java.util.Calendar
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.common.StrigaIpAddressProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.request.StrigaAddWhitelistedAddressRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaEnrichAccountRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaGetWhitelistedAddressesRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaInitWithdrawalRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaOnchainWithdrawalFeeRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaSmsResendRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaSmsVerifyRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaUserWalletsRequest
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaOnchainWithdrawalFees
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId

class StrigaWalletRemoteRepository(
    private val api: StrigaWalletApi,
    private val mapper: StrigaWalletRepositoryMapper,
    private val walletsMapper: StrigaUserWalletsMapper,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val ipAddressProvider: StrigaIpAddressProvider
) : StrigaWalletRepository {

    override suspend fun initiateOnchainWithdrawal(
        sourceAccountId: StrigaAccountId,
        whitelistedAddressId: StrigaWhitelistedAddressId,
        amountInUnits: BigInteger,
    ): StrigaDataLayerResult<StrigaInitWithdrawalDetails> {
        return try {
            val request = StrigaInitWithdrawalRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                sourceAccountId = sourceAccountId.value,
                whitelistedAddressId = whitelistedAddressId.value,
                amountInUnits = amountInUnits.toString()
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

    override suspend fun getOnchainWithdrawalFees(
        sourceAccountId: StrigaAccountId,
        whitelistedAddressId: StrigaWhitelistedAddressId,
        amount: BigInteger,
    ): StrigaDataLayerResult<StrigaOnchainWithdrawalFees> {
        return try {
            val request = StrigaOnchainWithdrawalFeeRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                sourceAccountId = sourceAccountId.value,
                whitelistedAddressId = whitelistedAddressId.value,
                amountInUnits = amount.toString()
            )
            val response = api.getOnchainWithdrawalFees(request)
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
                userId = strigaUserIdProvider.getUserIdOrThrow(),
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
                userId = strigaUserIdProvider.getUserIdOrThrow(),
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
                userId = strigaUserIdProvider.getUserIdOrThrow(),
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

    override suspend fun resendSms(
        challengeId: StrigaWithdrawalChallengeId
    ): StrigaDataLayerResult<Unit> {
        return try {
            val request = StrigaSmsResendRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                challengeId = challengeId.value
            )
            api.resendSms(request)
            return StrigaDataLayerResult.Success(Unit)
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }

    override suspend fun verifySms(
        smsCode: String,
        challengeId: StrigaWithdrawalChallengeId,
    ): StrigaDataLayerResult<Unit> {
        return try {
            val request = StrigaSmsVerifyRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                challengeId = challengeId.value,
                verificationCode = smsCode,
                ipAddress = ipAddressProvider.getIpAddress()
            )
            api.verifySms(request)
            return StrigaDataLayerResult.Success(Unit)
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }
}
