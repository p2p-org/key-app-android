package org.p2p.wallet.striga.wallet.repository.impl

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.utils.STRIGA_FIAT_DECIMALS
import org.p2p.core.utils.toLamports
import org.p2p.wallet.striga.common.StrigaIpAddressProvider
import org.p2p.wallet.striga.common.StrigaUserIdProvider
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.common.model.toSuccessResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.request.StrigaInitEurOffRampRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaInitWithdrawalRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaOnRampSmsResendRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaOnRampSmsVerifyRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaOnchainWithdrawalFeeRequest
import org.p2p.wallet.striga.wallet.models.StrigaInitEurOffRampDetails
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaOnchainWithdrawalFees
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.striga.wallet.repository.StrigaWithdrawalsRepository
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWithdrawalsMapper

private const val TAG = "StrigaWithdrawalsRepository"

class StrigaWithdrawalsRemoteRepository(
    private val api: StrigaWalletApi,
    private val mapper: StrigaWithdrawalsMapper,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val ipAddressProvider: StrigaIpAddressProvider
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

    override suspend fun initEurOffRamp(
        sourceAccountId: StrigaAccountId,
        amount: BigDecimal,
        iban: String,
        bic: String
    ): StrigaDataLayerResult<StrigaInitEurOffRampDetails> {
        return try {
            timber.i("startEurOffRamp started")
            val request = StrigaInitEurOffRampRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                sourceAccountId = sourceAccountId.value,
                amountInUnits = amount.toLamports(STRIGA_FIAT_DECIMALS).toString(),
                destination = StrigaInitEurOffRampRequest.BankingDetailsRequest(iban = iban, bic = bic)
            )
            val response = api.initiateEurOffRamp(request)
            return mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            timber.i(error, "startEurOffRamp failed")
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
            val request = StrigaOnRampSmsResendRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                challengeId = challengeId.value
            )
            api.withdrawalResendSms(request)
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
            val request = StrigaOnRampSmsVerifyRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                challengeId = challengeId.value,
                verificationCode = smsCode,
                ipAddress = ipAddressProvider.getIpAddress()
            )
            api.withdrawalVerifySms(request)
            StrigaDataLayerResult.Success(Unit)
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }
}
