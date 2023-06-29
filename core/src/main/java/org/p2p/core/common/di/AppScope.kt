package org.p2p.core.common.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

open class AppScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main
}
