package org.p2p.wallet.history.interactor.stream

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class AbstractStreamSource : HistoryStreamSource {

    override suspend fun nextItems(
        configuration: StreamSourceConfiguration
    ): List<HistoryStreamItem> {
        val sequence = mutableListOf<HistoryStreamItem>()
        withContext(Dispatchers.IO + NonCancellable + CoroutineExceptionHandler {_, t ->
            Timber.tag("FillBuffer").d("Main thread error $t")
        }) {
            while (true) {
                val item = next(configuration) ?: break
                sequence.add(item)
            }
        }
        return sequence
    }
}
