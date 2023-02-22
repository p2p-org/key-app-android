package org.p2p.wallet.swap.jupiter.statemanager

object SwapStateManagerHolder {

    private val swapStateManagerHolder = mutableMapOf<String, SwapStateManager>()

    fun getOrCreate(key: String, defaultValue: () -> SwapStateManager): SwapStateManager =
        swapStateManagerHolder.getOrPut(key) {
            defaultValue()
        }

    fun get(key: String): SwapStateManager = swapStateManagerHolder[key]!!

    fun clear(key: String) {
        swapStateManagerHolder[key]?.finishWork()
        swapStateManagerHolder.remove(key)
    }
}
