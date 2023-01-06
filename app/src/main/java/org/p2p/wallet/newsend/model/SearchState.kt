package org.p2p.wallet.newsend.model

import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.emptyString

data class SearchState(
    val query: String = emptyString(),
    val foundResult: List<SearchResult> = listOf(),
    val recentRecipients: List<SearchResult>? = null,
    val isLoading: Boolean = false,
) {

    sealed interface State {
        data class UsersFound(val query: String, val users: List<SearchResult>) : State
        data class UsersNotFound(val query: String) : State
        data class ShowInvalidAddresses(val users: List<SearchResult>) : State
        data class ShowRecipients(val recipients: List<SearchResult>) : State
        data class ShowLoadingState(val query: String) : State
        object ShowEmptyState : State
    }

    val state: State
        get() {
            return when {
                foundResult.any { it.isInvalid() } ->
                    State.ShowInvalidAddresses(foundResult)
                foundResult.isNotEmpty() ->
                    State.UsersFound(query, foundResult)
                isLoading && query.isNotEmpty() && foundResult.isEmpty() ->
                    State.ShowLoadingState(query)
                query.isNotEmpty() && foundResult.isEmpty() ->
                    State.UsersNotFound(query)
                !recentRecipients.isNullOrEmpty() ->
                    State.ShowRecipients(recentRecipients)
                else ->
                    State.ShowEmptyState
            }
        }

    fun updateLoading(isLoading: Boolean): SearchState = copy(isLoading = isLoading)

    fun updateSearchResult(newQuery: String, newResult: List<SearchResult>): SearchState {
        return SearchState(
            query = newQuery,
            foundResult = newResult,
            recentRecipients = recentRecipients,
            isLoading = false
        )
    }

    fun updateRecipients(newRecipients: List<SearchResult>): SearchState {
        return this.copy(recentRecipients = newRecipients, isLoading = false)
    }

    fun reset(): SearchState {
        return SearchState(
            query = emptyString(),
            foundResult = listOf(),
            recentRecipients = recentRecipients,
            isLoading = false
        )
    }
}
