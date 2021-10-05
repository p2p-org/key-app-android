package com.p2p.wallet.common

interface AppRestarter {

    companion object {
        operator fun invoke(restartCallback: () -> Unit) = object : AppRestarter {
            override fun restartApp() {
                restartCallback()
            }
        }
    }

    fun restartApp()
}