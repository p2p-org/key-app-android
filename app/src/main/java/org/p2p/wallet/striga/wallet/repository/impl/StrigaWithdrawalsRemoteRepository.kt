package org.p2p.wallet.striga.wallet.repository.impl

import timber.log.Timber
import java.math.BigInteger
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.request.StrigaInitWithdrawalRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaOnchainWithdrawalFeeRequest
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaOnchainWithdrawalFees
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.repository.StrigaWithdrawalsRepository
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWithdrawalsMapper

private const val TAG = "StrigaWithdrawalsRepository"

class StrigaWithdrawalsRemoteRepository(
    private val api: StrigaWalletApi,
    private val mapper: StrigaWithdrawalsMapper,
    private val strigaUserIdProvider: StrigaUserIdProvider,
) : StrigaWithdrawalsRepository {
    private val timber: Timber.Tree = Timber.tag(TAG)

    override suspend fun initiateOnchainWithdrawal(
        sourceAccountId: StrigaAccountId,
        whitelistedAddressId: StrigaWhitelistedAddressId,
        amountInUnits: BigInteger,
    ): StrigaDataLayerResult<StrigaInitWithdrawalDetails> {
        return try {
            timber.i("initiateOnchainWithdrawal started")
            val request = StrigaInitWithdrawalRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                sourceAccountId = sourceAccountId.value,
                whitelistedAddressId = whitelistedAddressId.value,
                amountInUnits = amountInUnits.toString()
            )
            val response = api.initiateOnchainWithdrawal(request)
            return mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            timber.i(error, "initiateOnchainWithdrawal failed")
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
            timber.i("getOnchainWithdrawalFees started")
            val request = StrigaOnchainWithdrawalFeeRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                sourceAccountId = sourceAccountId.value,
                whitelistedAddressId = whitelistedAddressId.value,
                amountInUnits = amount.toString()
            )
            val response = api.getOnchainWithdrawalFees(request)
            return mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            timber.i(error, "getOnchainWithdrawalFees failed")
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }
}
