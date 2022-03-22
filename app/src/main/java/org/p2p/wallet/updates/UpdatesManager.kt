package org.p2p.wallet.updates

interface UpdatesManager {
    fun start()

    /**
     * Stops updates manager. This action is terminal and updates manager can't be restarted after it.
     * If you need [stop]-[start] cycle - then use [restart] instead. It uses non-terminal stop inside.
     */
    fun stop()

    /**
     * Suspends until socket updates are stopped in non-terminal manner, then queues up [start] and returns.
     */
    suspend fun restart()

    fun subscribeToTransaction(signature: String)

    fun unsubscribeFromTransaction(signature: String)

    fun addUpdatesStateObserver(observer: UpdatesStateObserver)

    fun removeUpdatesStateObserver(observer: UpdatesStateObserver)
}
