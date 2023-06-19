package org.p2p.wallet.striga.user.repository

import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.user.StrigaStorageContract
import org.p2p.wallet.striga.user.model.StrigaUserStatus
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination

private const val TAG = "StrigaUserStatusRepository"

class StrigaUserStatusRepository(
    private val dispatchers: CoroutineDispatchers,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val mapper: StrigaUserStatusDestinationMapper,
    private val userRepository: StrigaUserRepository,
    private val strigaStorage: StrigaStorageContract,
    private val inAppFeatureFlags: InAppFeatureFlags
) : CoroutineScope by CoroutineScope(dispatchers.io) {

    private val strigaUserDestinationFlow = MutableStateFlow<StrigaUserStatusDestination?>(null)
    private val strigaBannerFlow = MutableStateFlow<StrigaKycStatusBanner?>(null)

    private val bannerMock: StrigaKycStatusBanner?
        get() = inAppFeatureFlags.strigaKycBannerMockFlag
            .featureValueString
            ?.let { StrigaKycStatusBanner.valueOf(it) }

    init {
        handleUserStatus(strigaStorage.userStatus)
    }

    fun getBannerFlow(): StateFlow<StrigaKycStatusBanner?> {
        return if (bannerMock != null) MutableStateFlow(bannerMock) else strigaBannerFlow
    }

    fun getUserDestination(): StrigaUserStatusDestination? = strigaUserDestinationFlow.value

    fun loadUserKycStatus() {
        launch {
            try {
                // null user status can be only in case if user is not created at all (no id),
                // otherwise we must be sure that user status is not null or throw an exception
                val userStatus = if (strigaUserIdProvider.getUserId() == null) {
                    null
                } else {
                    loadUserStatus()
                }
                strigaStorage.userStatus = userStatus
                handleUserStatus(userStatus)
            } catch (e: Throwable) {
                Timber.tag(TAG).e(e, "Error while fetching user kyc status")
            }
        }
    }

    @Throws(StrigaDataLayerError::class)
    private suspend fun loadUserStatus(): StrigaUserStatus {
        return userRepository.getUserStatus().unwrap()
    }

    private fun handleUserStatus(userStatus: StrigaUserStatus?) {
        strigaUserDestinationFlow.value = userStatus.let(::mapToDestination)
        strigaBannerFlow.value = userStatus.let(::mapToBanner)
    }

    private fun mapToDestination(status: StrigaUserStatus?): StrigaUserStatusDestination {
        return mapper.mapToDestination(
            userStatus = status,
            isUserCreated = strigaUserIdProvider.getUserId() != null
        )
    }

    private fun mapToBanner(status: StrigaUserStatus?): StrigaKycStatusBanner? {
        return mapper.mapToStatusBanner(status)
    }
}
