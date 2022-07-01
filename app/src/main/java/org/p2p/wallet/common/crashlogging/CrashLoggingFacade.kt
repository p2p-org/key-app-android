package org.p2p.wallet.common.crashlogging

interface CrashLoggingFacade {
    fun logInformation(information: String)
    fun logThrowable(error: Throwable, message: String? = null)

    fun setUserId(userId: String)
    fun clearUserId()

    fun setCustomKey(key: String, value: Any)
}
