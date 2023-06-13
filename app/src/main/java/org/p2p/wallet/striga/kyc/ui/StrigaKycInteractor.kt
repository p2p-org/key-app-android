package org.p2p.wallet.striga.kyc.ui

import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.user.repository.StrigaUserRepository

class StrigaKycInteractor(
    private val strigaUserRepository: StrigaUserRepository,
    private val metadataInteractor: MetadataInteractor
) {
    suspend fun obtainAccessToken(): StrigaDataLayerResult<String> = strigaUserRepository.getAccessToken()

    fun getUserEmail(): String =
        metadataInteractor.currentMetadata?.socialShareOwnerEmail
            ?: error("No verified WEB3 email to pass to start KYC")

    fun getUserPhone(): String =
        metadataInteractor.currentMetadata?.customSharePhoneNumberE164
            ?: error("No verified phone number to pass to start KYC")
}
