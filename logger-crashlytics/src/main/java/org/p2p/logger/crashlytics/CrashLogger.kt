package org.p2p.logger.crashlytics

class CrashLogger(
    @Suppress("DEPRECATION")
    private val crashLoggingFacades: List<CrashLoggingFacade>,
) {
    sealed interface UserId {
        companion object {
            operator fun invoke(value: String): UserId = if (value.isNotBlank()) Filled(value) else NotSet
        }

        class Filled(val value: String) : UserId
        object NotSet : UserId
    }

    fun logInformation(information: String) {
        crashLoggingFacades.forEach { it.logInformation(information) }
    }

    fun logThrowable(error: Throwable, message: String? = null) {
        crashLoggingFacades.forEach { it.logThrowable(error, message) }
    }

    fun setUserId(userId: String) {
        setUserId(UserId(userId))
    }

    private fun setUserId(userId: UserId) {
        when (userId) {
            is UserId.Filled -> crashLoggingFacades.forEach { it.setUserId(userId.value) }
            is UserId.NotSet -> crashLoggingFacades.forEach { it.clearUserId() }
        }
    }

    fun setCustomKey(key: String, value: Any) {
        crashLoggingFacades.forEach { it.setCustomKey(key, value) }
    }
}
