package org.p2p.wallet.striga.user.repository

import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.user.api.StrigaApi
import org.p2p.wallet.striga.user.api.request.StrigaResendSmsRequest
import org.p2p.wallet.striga.user.api.request.StrigaStartKycRequest
import org.p2p.wallet.striga.user.api.request.StrigaVerifyMobileNumberRequest
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails

class StrigaUserRemoteRepository(
    private val api: StrigaApi,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val mapper: StrigaUserRepositoryMapper
) : StrigaUserRepository {

    override suspend fun createUser(data: List<StrigaSignupData>): StrigaDataLayerResult<StrigaUserInitialDetails> {
        return try {
            val request = mapper.toNetwork(data)
            val response = api.createUser(request)
            mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }

    override suspend fun getUserDetails(): StrigaDataLayerResult<StrigaUserDetails> {
        return try {
            val response = api.getUserDetails(strigaUserIdProvider.getUserIdOrThrow())
            mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }

    override suspend fun getUserVerificationStatus(): StrigaDataLayerResult<StrigaUserStatusDetails> {
        return try {
            val response = api.getUserVerificationStatus(strigaUserIdProvider.getUserIdOrThrow())
            mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }

    override suspend fun verifyPhoneNumber(verificationCode: String): StrigaDataLayerResult<Unit> {
        return try {
            val request = StrigaVerifyMobileNumberRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
                verificationCode = verificationCode
            )
            api.verifyMobileNumber(request)
            StrigaDataLayerResult.Success(Unit)
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }

    override suspend fun resendSmsForVerifyPhoneNumber(): StrigaDataLayerResult<Unit> {
        return try {
            val request = StrigaResendSmsRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
            )
            api.resendSms(request)
            StrigaDataLayerResult.Success(Unit)
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }

    override suspend fun getAccessToken(): StrigaDataLayerResult<String> {
        return try {
            val request = StrigaStartKycRequest(
                userId = strigaUserIdProvider.getUserIdOrThrow(),
            )
            StrigaDataLayerResult.Success(api.getAccessToken(request).accessToken)
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }
}
