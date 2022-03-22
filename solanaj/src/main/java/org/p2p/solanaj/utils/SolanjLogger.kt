package org.p2p.solanaj.utils

/**
 * Can't use Timber in this module, but sometimes logging is needed
 * so we've created an interface and have it's implementation in main :app
 */
interface SolanjLogger {
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