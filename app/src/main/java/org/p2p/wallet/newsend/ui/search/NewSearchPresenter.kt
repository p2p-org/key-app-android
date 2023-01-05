package org.p2p.wallet.newsend.ui.search

import org.p2p.core.token.Token
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.newsend.model.SearchState
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SearchTarget
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DELAY_IN_MS = 250L

class NewSearchPresenter(
    private val initialToken: Token.Active?,
    private val searchInteractor: SearchInteractor,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle,
    private val userInteractor: UserInteractor,
    private val newSendAnalytics: NewSendAnalytics
) : BasePresenter<NewSearchContract.View>(), NewSearchContract.Presenter {

    private var state = MutableStateFlow(SearchState())
    private var searchJob: Job? = null

    override fun attach(view: NewSearchContract.View) {
        super.attach(view)
        newSendAnalytics.logNewSearchScreenOpened()

        if (state.value.recentRecipients == null) {
            loadRecentRecipients()
        } else {
            renderCurrentState()
        }
    }

    private fun loadRecentRecipients() {
        launch {
            val recipients = userInteractor.getRecipients()
            state.update { it.updateRecipients(recipients) }

            renderCurrentState()
        }
    }

    private fun renderCurrentState() {
        when (val currentState = state.value.state) {
            is SearchState.State.UsersFound -> {
                view?.showUsers(currentState.users)
                view?.showUsersMessage(R.string.search_found)
                view?.updateSearchInput(currentState.query, submit = false)
                view?.showBackgroundVisible(isVisible = true)
            }
            is SearchState.State.UsersNotFound -> {
                view?.showNotFound()
                view?.showUsersMessage(null)
                view?.updateSearchInput(currentState.query, submit = false)
                view?.showBackgroundVisible(isVisible = true)
            }
            is SearchState.State.ShowInvalidAddresses -> {
                view?.showUsers(currentState.users)
                view?.showUsersMessage(R.string.search_found)
                view?.showBackgroundVisible(isVisible = false)
            }
            is SearchState.State.ShowRecipients -> {
                view?.showUsers(currentState.recipients)
                view?.showUsersMessage(R.string.search_recently)
                view?.showBackgroundVisible(isVisible = true)
            }
            is SearchState.State.ShowEmptyState -> {
                view?.showEmptyState(isEmpty = true)
                view?.showUsersMessage(null)
                view?.clearUsers()
                view?.updateSearchInput("", submit = false)
                view?.showBackgroundVisible(isVisible = true)
            }
            is SearchState.State.ShowLoadingState -> {
                view?.showUsersMessage(null)
                view?.updateSearchInput(currentState.query, submit = false)
                view?.showBackgroundVisible(isVisible = true)
                view?.showLoading()
            }
        }
    }

    override fun search(newQuery: String) {
        // when screen is restored, the searchView triggers the queryChange automatically
        // we don't need to make a new request, since we already restored the state in attach
        if (state.value.query == newQuery) {
            return
        }

        if (newQuery.isBlank()) {
            searchJob?.cancel()
            state.update { it.clear() }
            renderCurrentState()
            return
        }

        state.update { it.updateSearchResult(newQuery, it.foundResult) }

        val target = SearchTarget(
            value = newQuery,
            keyAppDomainIfUsername = usernameDomainFeatureToggle.value
        )

        searchJob?.cancel()
        searchJob = launch {
            try {
                showSkeleton(true)
                state.update { it.updateSearchResult(newQuery, emptyList()) }
                delay(DELAY_IN_MS)
                validateAndSearch(target)
            } catch (e: CancellationException) {
                Timber.i("Cancelled search target validation: ${target.value}")
            } catch (e: Throwable) {
                Timber.e(e, "Error searching target: $newQuery")
                validateOnlyAddress(target)
            } finally {
                showSkeleton(false)
            }
        }
    }

    override fun onSearchResultClick(result: SearchResult) {
        checkPreselectedTokenAndSubmitResult(result)
    }

    override fun onScanClicked() {
        view?.showScanner()
    }

    fun checkPreselectedTokenAndSubmitResult(result: SearchResult) {
        launch {
            val finalResult: SearchResult
            val preselectedToken: Token.Active?
            if (result is SearchResult.AddressFound) {
                val balance = userInteractor.getBalance(result.addressState.address.toBase58Instance())
                finalResult = result.copyWithBalance(balance)
                preselectedToken = result.sourceToken ?: initialToken
            } else {
                finalResult = result
                preselectedToken = initialToken
            }

            logRecipientSelected(finalResult)

            view?.submitSearchResult(finalResult, preselectedToken)
        }
    }

    private suspend fun validateAndSearch(target: SearchTarget) {
        when (target.validation) {
            SearchTarget.Validation.USERNAME -> searchByUsername(target.trimmedUsername)
            SearchTarget.Validation.SOLANA_TYPE_ADDRESS -> searchBySolAddress(target.value)
            SearchTarget.Validation.EMPTY -> renderCurrentState()
            else -> showNotFound()
        }
    }

    private suspend fun validateOnlyAddress(target: SearchTarget) {
        when (target.validation) {
            SearchTarget.Validation.SOLANA_TYPE_ADDRESS -> searchBySolAddress(target.value)
            else -> view?.showErrorState()
        }
    }

    private suspend fun searchByUsername(username: String) {
        val usernames = searchInteractor.searchByName(username)
        state.update { it.updateSearchResult(username, usernames) }
        renderCurrentState()
    }

    private suspend fun searchBySolAddress(address: String) {
        val publicKey = try {
            PublicKey(address)
        } catch (e: Throwable) {
            Timber.i(e)
            state.update { it.updateSearchResult(address, emptyList()) }
            renderCurrentState()
            return
        }

        val newAddresses = searchInteractor.searchByAddress(publicKey.toBase58().toBase58Instance(), initialToken)
        state.update { it.updateSearchResult(address, listOf(newAddresses)) }
        renderCurrentState()
    }

    private fun showNotFound() {
        view?.showUsersMessage(null)
        view?.clearUsers()
        view?.showNotFound()
    }

    private fun showSkeleton(show: Boolean) {
        state.update { it.updateLoading(show) }
        renderCurrentState()
    }

    private fun logRecipientSelected(recipient: SearchResult) {
        newSendAnalytics.logRecipientSelected(recipient, state.value.foundResult)
    }
}
