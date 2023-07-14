package org.p2p.wallet.striga.user.repository

import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.user.StrigaStorageContract
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails

private const val TAG = "StrigaUserStatusRepository"

class StrigaUserStatusRepository(
    private val dispatchers: CoroutineDispatchers,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val mapper: StrigaUserStatusDestinationMapper,
    private val strigaUserRepository: StrigaUserRepository,
    private val strigaStorage: StrigaStorageContract,
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val strigaSignupEnabledFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val strigaUserRepositoryMapper: StrigaUserRepositoryMapper,
) : CoroutineScope by CoroutineScope(dispatchers.io) {

    private var strigaUserDestination: StrigaUserStatusDestination = StrigaUserStatusDestination.NONE
    private val strigaBannerFlow = MutableStateFlow<StrigaKycStatusBanner?>(null)

    private val bannerMock: StrigaKycStatusBanner?
        get() = inAppFeatureFlags.strigaKycBannerMockFlag
            .featureValueString
            ?.let { StrigaKycStatusBanner.valueOf(it) }

    init {
        mapUserStatusToFlows(strigaStorage.userStatus)
    }

    fun getBannerFlow(): StateFlow<StrigaKycStatusBanner?> {
        if (!strigaSignupEnabledFeatureToggle.isFeatureEnabled) return MutableStateFlow(null)

        return if (bannerMock != null) MutableStateFlow(bannerMock) else strigaBannerFlow
    }

    fun getUserDestination(): StrigaUserStatusDestination = strigaUserDestination

    fun getUserVerificationStatus(): StrigaUserStatusDetails? = strigaStorage.userStatus

    fun updateUserStatus(response: StrigaUserInitialDetails) {
        strigaStorage.userStatus = strigaUserRepositoryMapper.mapUserInitialDetailsToStatus(response)
        mapUserStatusToFlows(strigaStorage.userStatus)
    }

    suspend fun loadAndSaveUserKycStatus(): StrigaDataLayerResult<Unit> = withContext(coroutineContext) {
        try {
            // null user status can be only in case if user is not created at all (no id),
            // otherwise we must be sure that user status is not null or throw an exception
            val userStatus = if (strigaUserIdProvider.getUserId() == null) {
                null
            } else {
                strigaUserRepository.getUserVerificationStatus().unwrap()
            }
            strigaStorage.userStatus = userStatus
            mapUserStatusToFlows(userStatus)
            Unit.toSuccessResult()
        } catch (e: Throwable) {
            Timber.tag(TAG).i(e, "Error while fetching user kyc status")
            StrigaDataLayerError.from(
                error = e,
                default = StrigaDataLayerError.InternalError(e, "Error while fetching user kyc status")
            )
        }
    }

    private fun mapUserStatusToFlows(userStatus: StrigaUserStatusDetails?) {
        strigaUserDestination = userStatus.let(mapper::mapToDestination)
        strigaBannerFlow.value = userStatus.let(mapper::mapToStatusBanner)
            ?.takeUnless { strigaStorage.isBannerHidden(it) }
    }

    fun hideBanner(banner: StrigaKycStatusBanner) {
        strigaStorage.hideBanner(banner)
        strigaBannerFlow.value = null
    }
}
