package org.p2p.wallet.utils

import timber.log.Timber
import org.p2p.solanaj.utils.SolanjLogger

private const val SOLANAJ_TAG = "SOLANAJ_MODULE"
class SolanajTimberLogger : SolanjLogger.Logger {

    override fun d(throwable: Throwable) = Timber.tag(SOLANAJ_TAG).d(throwable)
    override fun d(message: String) = Timber.tag(SOLANAJ_TAG).d(message)
    override fun d(throwable: Throwable, message: String?) = Timber.tag(SOLANAJ_TAG).d(throwable, message)

    override fun w(throwable: Throwable) = Timber.tag(SOLANAJ_TAG).w(throwable)
    override fun w(message: String) = Timber.tag(SOLANAJ_TAG).w(message)
    override fun w(throwable: Throwable, message: String?) = Timber.tag(SOLANAJ_TAG).w(throwable, message)

    override fun e(throwable: Throwable) = Timber.tag(SOLANAJ_TAG).e(throwable)
    override fun e(message: String) = Timber.tag(SOLANAJ_TAG).e(message)
    override fun e(throwable: Throwable, message: String?) = Timber.tag(SOLANAJ_TAG).e(throwable, message)
}
