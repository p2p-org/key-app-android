package org.p2p.wallet.history.interactor

class HistoryFetchListResult<T>(
    content: MutableList<T> = mutableListOf(),
    val isFailed: Boolean = false,
) {
    private val innerContent = content

    private var contentFilter: ((T) -> Boolean)? = null

    val content: List<T>
        get() = contentFilter?.let(innerContent::filter) ?: innerContent

    fun withContentFilter(filter: (T) -> Boolean): HistoryFetchListResult<T> = apply {
        contentFilter = filter
    }

    fun hasFetchedItems(): Boolean = content.isNotEmpty()

    fun clearContent() {
        innerContent.clear()
    }

    operator fun plus(other: HistoryFetchListResult<T>): HistoryFetchListResult<T> {
        val result = HistoryFetchListResult(
            content = (innerContent + other.innerContent).toMutableList(),
            isFailed = other.isFailed
        )
        return other.contentFilter?.let(result::withContentFilter) ?: result
    }
}
