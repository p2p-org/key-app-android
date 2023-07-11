package org.p2p.wallet.striga.user.interactor

import kotlinx.coroutines.flow.StateFlow
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.user.repository.StrigaUserRepository
import org.p2p.wallet.striga.user.repository.StrigaUserStatusRepository

class StrigaUserInteractor(
    private val userRepository: StrigaUserRepository,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val userStatusRepository: StrigaUserStatusRepository,
    private val strigaSignupDataRepository: StrigaSignupDataLocalRepository
) {
    val isKycApproved: Boolean
        get() = userStatusRepository.getUserVerificationStatus()?.isKycApproved == true

    suspend fun createUser(data: List<StrigaSignupData>): StrigaDataLayerResult<StrigaUserInitialDetails> {
        return userRepository.createUser(data)
    }

    suspend fun getUserDetails(): StrigaDataLayerResult<StrigaUserDetails> {
        return userRepository.getUserDetails()
    }

    fun isUserCreated(): Boolean = strigaUserIdProvider.getUserId() != null

    suspend fun isUserDetailsLoaded(): Boolean {
        return strigaSignupDataRepository.getUserSignupData().unwrap().isEmpty()
    }

    fun isUserVerificationStatusLoaded(): Boolean = userStatusRepository.getUserVerificationStatus() != null

    suspend fun loadAndSaveUserStatusData(): StrigaDataLayerResult<Unit> {
        return userStatusRepository.loadAndSaveUserKycStatus()
    }

    fun getUserStatusBannerFlow(): StateFlow<StrigaKycStatusBanner?> {
        return userStatusRepository.getBannerFlow()
    }

    fun hideUserStatusBanner(banner: StrigaKycStatusBanner) {
        userStatusRepository.hideBanner(banner)
    }

    /**
     * Returns user destination based on user status or NONE if unable to detect (no user status)
     */
    fun getUserDestination(): StrigaUserStatusDestination {
        return userStatusRepository.getUserDestination()
    }
}
