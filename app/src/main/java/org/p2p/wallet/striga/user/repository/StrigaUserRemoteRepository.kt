package org.p2p.wallet.striga.user.repository

import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.user.api.StrigaApi
import org.p2p.wallet.striga.user.api.StrigaResendSmsRequest
import org.p2p.wallet.striga.user.api.StrigaVerifyMobileNumberRequest
import org.p2p.wallet.striga.user.model.StrigaUserDetails

class StrigaUserRemoteRepository(
    private val api: StrigaApi,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val mapper: StrigaUserRepositoryMapper
) : StrigaUserRepository {
    override suspend fun getUserDetails(): StrigaDataLayerResult<StrigaUserDetails> {
        return try {
            val response = api.getUserDetails(strigaUserIdProvider.getUserId())
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
                userId = strigaUserIdProvider.getUserId(),
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
                userId = strigaUserIdProvider.getUserId(),
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
}
