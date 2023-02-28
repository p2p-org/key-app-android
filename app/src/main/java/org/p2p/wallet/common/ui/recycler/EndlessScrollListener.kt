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

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

        if (totalItemCount == visibleItemCount) return

        if (totalItemCount > totalLoadedItems) {
            isLoading = false
            totalLoadedItems = totalItemCount
            return
        }

        val smth = totalItemCount - visibleItemCount <= firstVisibleItem + VISIBLE_THRESHOLD
        if (!isLoading && smth) {
            loadNextPage(totalItemCount)
            isLoading = true
        }
    }

    fun reset() {
        isLoading = false
        totalLoadedItems = 0
    }
}
