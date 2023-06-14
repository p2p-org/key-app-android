package org.p2p.wallet.striga.user.repository

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.user.model.StrigaUserStatus
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination

private const val DELAY_IM_MILLISECONDS = 10_000L
private const val TAG = "StrigaUserStatusRepository"

class StrigaUserStatusRepository(
    private val dispatchers: CoroutineDispatchers,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val mapper: StrigaUserStatusDestinationMapper,
    private val userRepository: StrigaUserRepository,
) : CoroutineScope {

    init {
        loadUserKycStatus()
    }

    override val coroutineContext: CoroutineContext
        get() = dispatchers.io

    private val strigaUserDestinationFlow = MutableStateFlow(StrigaUserStatusDestination.NONE)

    fun getUserDestinationFlow(): Flow<StrigaUserStatusDestination> = strigaUserDestinationFlow

    fun getUserDestination(): StrigaUserStatusDestination = strigaUserDestinationFlow.value

    private fun loadUserKycStatus() {
        launch {
            while (isActive) {
                try {
                    val kycStatusDestination = loadUserStatus().let(::mapToDestination)
                    strigaUserDestinationFlow.value = kycStatusDestination
                    delay(DELAY_IM_MILLISECONDS)
                } catch (e: Throwable) {
                    Timber.tag(TAG).e(e, "Error while fetching user kyc status")
                }
            }
        }
    }

    private suspend fun loadUserStatus(): StrigaUserStatus? {
        return userRepository.getUserStatus().successOrNull()
    }

    private fun mapToDestination(status: StrigaUserStatus?): StrigaUserStatusDestination {
        return mapper.mapToDestination(
            userStatus = status,
            isUserCreated = strigaUserIdProvider.getUserId() != null
        )
    }
}
