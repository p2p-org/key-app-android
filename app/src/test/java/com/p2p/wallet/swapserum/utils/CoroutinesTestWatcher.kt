package com.p2p.wallet.swapserum.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class CoroutinesTestWatcher : TestWatcher() {

    private val mainThreadSurrogate = Dispatchers.Unconfined

    override fun starting(description: Description?) {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    override fun finished(description: Description?) {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }
}