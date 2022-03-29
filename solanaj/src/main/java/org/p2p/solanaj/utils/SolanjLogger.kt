package org.p2p.solanaj.utils

/**
 * Can't use Timber in this module, but sometimes logging is needed
 * so we've created an interface and have it's implementation in main :app
 */
object SolanjLogger {

    interface Logger {
        fun d(throwable: Throwable)
        fun d(message: String)
        fun d(throwable: Throwable, message: String?)

        fun w(throwable: Throwable)
        fun w(message: String)
        fun w(throwable: Throwable, message: String?)

        fun e(throwable: Throwable)
        fun e(message: String)
        fun e(throwable: Throwable, message: String?)
    }

    private var innerLogger: Logger? = null

    fun setLoggerImplementation(logger: Logger) {
        innerLogger = logger
    }

    fun d(throwable: Throwable) {
        innerLogger?.d(throwable)
    }

    fun d(message: String) {
        innerLogger?.d(message)
    }

    fun d(throwable: Throwable, message: String?) {
        innerLogger?.d(throwable, message)
    }

    fun w(throwable: Throwable) {
        innerLogger?.w(throwable)
    }

    fun w(message: String) {
        innerLogger?.w(message)
    }

    fun w(throwable: Throwable, message: String?) {
        innerLogger?.w(throwable, message)
    }

    fun e(throwable: Throwable) {
        innerLogger?.e(throwable)
    }

    fun e(message: String) {
        innerLogger?.e(message)
    }

    fun e(throwable: Throwable, message: String?) {
        innerLogger?.e(throwable, message)
    }
}
