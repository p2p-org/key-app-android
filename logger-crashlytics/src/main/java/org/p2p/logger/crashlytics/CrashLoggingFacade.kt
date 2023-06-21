package org.p2p.logger.crashlytics

@Deprecated("Do not inject implementations in your code, inject only CrashLogger")
interface CrashLoggingFacade {
    fun logInformation(information: String)
    fun logThrowable(error: Throwable, message: String? = null)

    fun setUserId(userId: String)
    fun clearUserId()

    fun setCustomKey(key: String, value: Any)
}
