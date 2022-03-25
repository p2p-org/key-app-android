package org.p2p.wallet.history.ui.history.adapter

import org.p2p.wallet.common.ui.recycler.PagingState

class HistoryAdapterPagingController(
    private val adapter: HistoryAdapter
) {
    var currentPagingState: PagingState = PagingState.Idle

    private companion object {
        private val noAdditionalItemRequiredState = listOf(PagingState.Idle)
    }

    fun setPagingState(newState: PagingState) {
        if (currentPagingState == newState) return

        val shouldHasExtraItem = stateRequiresLoadingItem(newState)
        val hasExtraItem = stateRequiresLoadingItem(currentPagingState)

        currentPagingState = newState
        // since item count is a function - cache its value.
        val count = adapter.itemCount
        when {
            hasExtraItem && shouldHasExtraItem -> adapter.notifyItemChanged(count - 1)
            hasExtraItem && !shouldHasExtraItem -> adapter.notifyItemRemoved(count - 1)
            !hasExtraItem && shouldHasExtraItem -> adapter.notifyItemInserted(count)
        }
    }

    fun stateRequiresLoadingItem(state: PagingState = currentPagingState): Boolean =
        state !in noAdditionalItemRequiredState

    fun isPagingInProgress(): Boolean =
        currentPagingState is PagingState.Loading || currentPagingState is PagingState.InitialLoading
}
