package com.p2p.wallet.common.recycler

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val VISIBLE_THRESHOLD = 11

class EndlessScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val loadNextPage: (Int) -> Unit
) : RecyclerView.OnScrollListener() {

    private var isLoading = false
    private var total = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = recyclerView.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

        if (totalItemCount == visibleItemCount) return

        if (isLoading && totalItemCount > total) {
            isLoading = false
            total = totalItemCount
        }

        if (!isLoading && totalItemCount - visibleItemCount <= firstVisibleItem + VISIBLE_THRESHOLD) {
            loadNextPage(totalItemCount)
            isLoading = true
        }
    }
}