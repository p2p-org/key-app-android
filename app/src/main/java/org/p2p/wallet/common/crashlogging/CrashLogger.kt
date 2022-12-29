package org.p2p.wallet.common.crashlogging

import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class CrashLogger(
    @Suppress("DEPRECATION")
    private val crashLoggingFacades: List<CrashLoggingFacade>,
    private val tokenKeyProvider: TokenKeyProvider
) {
    sealed interface UserId {
        companion object {
            operator fun invoke(value: String): UserId = if (value.isNotBlank()) Filled(value) else NotSet
        }

        class Filled(val value: String) : UserId
        object NotSet : UserId
    }

    init {
        val currentUserPublicKey = runCatching(tokenKeyProvider::publicKey).getOrDefault("")
        setUserId(UserId(currentUserPublicKey))

        tokenKeyProvider.registerListener { newUserPublicKey -> setUserId(UserId(newUserPublicKey)) }
    }

    fun logInformation(information: String) {
        crashLoggingFacades.forEach { it.logInformation(information) }
    }

    fun logThrowable(error: Throwable, message: String? = null) {
        crashLoggingFacades.forEach { it.logThrowable(error, message) }
    }

    fun setUserId(userId: UserId) {
        when (userId) {
            is UserId.Filled -> crashLoggingFacades.forEach { it.setUserId(userId.value) }
            is UserId.NotSet -> crashLoggingFacades.forEach { it.clearUserId() }
        }
    }

    fun setCustomKey(key: String, value: Any) {
        crashLoggingFacades.forEach { it.setCustomKey(key, value) }
    }
}
