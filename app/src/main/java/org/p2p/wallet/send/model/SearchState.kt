package org.p2p.wallet.send.model

import org.p2p.wallet.utils.emptyString

data class SearchState(
    var query: String = emptyString(),
    val foundResult: MutableList<SearchResult> = mutableListOf(),
    var recentRecipients: List<SearchResult>? = null
) {

    sealed interface ViewState {
        class UsersFound(val query: String, val users: List<SearchResult>) : ViewState
        class UsersNotFound(val query: String) : ViewState
        class ShowInvalidAddresses(val users: List<SearchResult>) : ViewState
        class ShowRecipients(val recipients: List<SearchResult>) : ViewState
        object ShowEmptyState : ViewState
    }

    val state: ViewState
        get() {
            return when {
                foundResult.any { it.isInvalid() } -> {
                    ViewState.ShowInvalidAddresses(foundResult)
                }
                foundResult.isNotEmpty() -> {
                    ViewState.UsersFound(query, foundResult)
                }
                query.isNotEmpty() && foundResult.isEmpty() -> {
                    ViewState.UsersNotFound(query)
                }
                !recentRecipients.isNullOrEmpty() -> {
                    ViewState.ShowRecipients(recentRecipients!!)
                }
                else -> {
                    ViewState.ShowEmptyState
                }
            }
        }

    fun updateSearchResult(newQuery: String, newResult: List<SearchResult>) {
        query = newQuery

        foundResult.clear()
        foundResult += newResult
    }

    fun updateRecipients(newRecipients: List<SearchResult>) {
        recentRecipients = newRecipients
    }

    fun clear() {
        this.query = emptyString()
        this.foundResult.clear()
    }
}
