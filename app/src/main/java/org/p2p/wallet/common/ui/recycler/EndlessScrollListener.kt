package org.p2p.wallet.common.ui.recycler

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val VISIBLE_THRESHOLD = 5

class EndlessScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val onYScroll: ((Int) -> Unit)? = null,
    private val loadNextPage: (Int) -> Unit,
) : RecyclerView.OnScrollListener() {

    private var isLoading = true
    private var totalLoadedItems = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        onYScroll?.invoke(dy)

        val visibleItemCount = recyclerView.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItem = layoutManager.findLastVisibleItemPosition()

        if (totalItemCount == visibleItemCount) return
        if (isLoading && totalItemCount > totalLoadedItems) {
            isLoading = false
            totalLoadedItems = totalItemCount
        }
        if (!isLoading && totalItemCount - visibleItemCount <= firstVisibleItem + VISIBLE_THRESHOLD) {
            loadNextPage.invoke(totalLoadedItems)
            isLoading = true
        }
    }

    fun reset() {
        isLoading = false
        totalLoadedItems = 0
    }
}
