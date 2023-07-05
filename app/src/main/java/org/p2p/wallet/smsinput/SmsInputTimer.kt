package org.p2p.wallet.smsinput

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.p2p.core.common.di.AppScope

class SmsInputTimer(
    private val appScope: AppScope
) {
    private var smsTimerStartSeconds = listOf(30, 40, 60, 90, 120)

    var smsResendCount = 0
        private set

    val isTimerActive: Boolean get() = timerJob?.isActive == true

    private val currentTimer: Int get() = smsTimerStartSeconds[smsResendCount % smsTimerStartSeconds.size]
    private var timerJob: Job? = null
    private val sharedTimer = MutableSharedFlow<Int>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    ).apply { tryEmit(0) }

    val smsInputTimerFlow: Flow<Int>
        get() = sharedTimer.distinctUntilChanged()

    fun startSmsInputTimerFlow() {
        timerJob?.cancel()
        timerJob = createSmsInputTimer(currentTimer)
        ++smsResendCount
    }

    fun resetSmsCount() {
        smsResendCount = 0
    }

    private fun createSmsInputTimer(
        timerSeconds: Int
    ): Job = (timerSeconds downTo 0).asSequence()
        .asFlow()
        .onEach {
            sharedTimer.tryEmit(it)
            delay(1.seconds.inWholeMilliseconds)
        }.launchIn(appScope)
}
