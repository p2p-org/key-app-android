package org.p2p.wallet.jupiter.statemanager

import timber.log.Timber

class SwapStateManagerHolder {

    private val swapStateManagerHolder = mutableMapOf<String, SwapStateManager>()

    fun getOrCreate(key: String, defaultValue: () -> SwapStateManager): SwapStateManager {
        return swapStateManagerHolder.getOrPut(key) {
            Timber.i("Swap state manager not found for key $key; creating new")
            defaultValue()
        }
    }

    fun get(key: String): SwapStateManager = swapStateManagerHolder[key]!!

    fun clear(key: String) {
        Timber.i("Clearing swap state manager key=$key; managers now=${swapStateManagerHolder.keys}")
        swapStateManagerHolder[key]?.finishWork()
        swapStateManagerHolder.remove(key)
    }
}
