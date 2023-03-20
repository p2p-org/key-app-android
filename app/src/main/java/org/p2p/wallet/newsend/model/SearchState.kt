package org.p2p.wallet.newsend.model

import org.p2p.wallet.utils.emptyString

data class SearchState(
    var query: String = emptyString(),
    val foundResult: MutableList<SearchResult> = mutableListOf(),
    var recentRecipients: List<SearchResult>? = null
) {

    sealed interface State {
        class UsersFound(val query: String, val users: List<SearchResult>) : State
        class UsersNotFound(val query: String) : State
        class ShowInvalidAddresses(val users: List<SearchResult>) : State
        class ShowRecipients(val recipients: List<SearchResult>) : State
        object ShowEmptyState : State
    }

    val state: State
        get() {
            return when {
                foundResult.any { it.isInvalid() } ->
                    State.ShowInvalidAddresses(foundResult)
                foundResult.isNotEmpty() ->
                    State.UsersFound(query, foundResult)
                query.isNotEmpty() && foundResult.isEmpty() ->
                    State.UsersNotFound(query)
                !recentRecipients.isNullOrEmpty() ->
                    State.ShowRecipients(recentRecipients!!)
                else ->
                    State.ShowEmptyState
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
