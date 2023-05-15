package org.p2p.core.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun Lifecycle.launchRestartable(
    jobsLifetime: ClosedRange<Lifecycle.Event> = Lifecycle.Event.ON_START..Lifecycle.Event.ON_STOP,
    unsubscribeOn: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
    scope: CoroutineScope = coroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) {
    LifecycleScopeRestarter(jobsLifetime, unsubscribeOn, scope, context, start, block)
        .also { addObserver(it) }
}

private class LifecycleScopeRestarter(
    private val jobsLifetime: ClosedRange<Lifecycle.Event>,
    private val unsubscribeOn: Lifecycle.Event,
    private val scope: CoroutineScope,
    private val context: CoroutineContext,
    private val start: CoroutineStart,
    private val block: suspend CoroutineScope.() -> Unit
) : LifecycleEventObserver {

    private val jobs = mutableListOf<Job>()

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (jobsLifetime.start == event) {
            jobs += scope.launch(context, start) { block() }
        } else if (jobsLifetime.endInclusive == event) {
            jobs.forEach { it.cancel() }
            jobs.clear()
        }

        if (unsubscribeOn == event) {
            source.lifecycle.removeObserver(this)
        }
    }
}
