package org.p2p.wallet.common.crashlytics

interface CrashLoggingService {

    sealed interface UserId {
        companion object {
            operator fun invoke(value: String): UserId {
                return if (value.isNotBlank()) Filled(value) else NotSet
            }
        }

        class Filled(val value: String) : UserId
        object NotSet : UserId
    }

    var isLoggingEnabled: Boolean

    fun logInformation(information: String)
    fun logThrowable(error: Throwable, message: String? = null)
    fun setUserId(userId: UserId)
    fun setCustomKey(key: String, value: Any)
}
