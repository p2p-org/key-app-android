package org.p2p.wallet.utils

import kotlin.time.DurationUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

class FlowDurationTimer(
    private val startValue: Long,
    private val durationUnit: DurationUnit
) {
    private var timerJob: Job? = null

    private var onEachAction: ((Long) -> Unit)? = null

    private var onTimerFinished: (() -> Unit)? = null

    fun onEach(action: (Long) -> Unit): FlowDurationTimer {
        return apply { onEachAction = action }
    }

    fun onTimeFinished(action: () -> Unit): FlowDurationTimer {
        return apply { onTimerFinished = action }
    }

    fun launchTimer(scope: CoroutineScope) {
        timerJob = (startValue downTo 0)
            .asSequence()
            .asFlow()
            .onEach {
                Timber.tag("_____").d(" start Delay value =$it ")
                onEachAction?.invoke(it)
                delay(1.minutes.inWholeMilliseconds)
                Timber.tag("_____").d("Delay value =$it ")
            }
            .onCompletion {
                onTimerFinished?.invoke()
                Timber.tag("_____").d("Timber finished")
            }
            .launchIn(scope)
    }

    fun stopTimer() {
        timerJob?.cancel()
    }
}
