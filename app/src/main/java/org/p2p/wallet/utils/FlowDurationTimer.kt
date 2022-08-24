package org.p2p.wallet.utils

import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

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
        timerJob =
            (startValue until 0)
                .asSequence()
                .asFlow()
                .onEach {
                    onEachAction?.invoke(it)
                    delay(1.toDuration(durationUnit).inWholeMilliseconds)
                }
                .onCompletion { onTimerFinished?.invoke() }
                .launchIn(scope)
    }

    fun stopTimer() {
        timerJob?.cancel()
    }
}
