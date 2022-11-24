package org.p2p.wallet.send.ui.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SearchTarget
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

private const val DELAY_IN_MS = 250L

class NewSearchPresenter(
    private val usernames: List<SearchResult>?,
    private val searchInteractor: SearchInteractor,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle
) : BasePresenter<NewSearchContract.View>(), NewSearchContract.Presenter {

    private var searchJob: Job? = null

    override fun loadInitialData() {
        val data = usernames
        if (data.isNullOrEmpty()) {
            // TODO PWN-6077 check latest recipients and show if exists
            view?.showEmptyState(isEmpty = true)
        } else {
            val value = (data.first() as SearchResult.UsernameFound).username
            view?.showMessage(R.string.search_found)
            view?.showSearchResult(data)
            view?.showSearchValue(value)
        }
    }

    override fun onContinueClicked(query: String) {
        onSearchResultClick(SearchResult.AddressOnly(addressState = AddressState(query)))
    }

    override fun search(newQuery: String) {
        val target = SearchTarget(
            value = newQuery,
            keyAppDomainIfUsername = usernameDomainFeatureToggle.value
        )

        searchJob?.cancel()
        searchJob = launch {
            try {
                delay(DELAY_IN_MS)
                view?.showLoading(isLoading = true)
                validateAndSearch(target)
            } catch (e: CancellationException) {
                Timber.i("Cancelled search target validation: ${target.value}")
            } catch (e: Throwable) {
                Timber.e(e, "Error searching target: $newQuery")
                validateOnlyAddress(target)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun onSearchResultClick(result: SearchResult) {
        if (searchInteractor.isOwnPublicKey(result.addressState.address)) {
            view?.showMessage(R.string.main_send_to_yourself_error)
        } else {
            view?.submitSearchResult(result)
        }
    }

    override fun onScanClicked() {
        view?.showScanner()
    }

    private suspend fun validateAndSearch(target: SearchTarget) {
        when (target.validation) {
            SearchTarget.Validation.USERNAME -> searchByUsername(target.trimmedUsername)
            SearchTarget.Validation.SOL_ADDRESS -> searchBySolAddress(target.value)
            SearchTarget.Validation.EMPTY -> showEmptyState()
            else -> showNotFound()
        }
    }

    private suspend fun validateOnlyAddress(target: SearchTarget) {
        when (target.validation) {
            SearchTarget.Validation.SOL_ADDRESS -> searchBySolAddress(target.value)
            else -> view?.showErrorState()
        }
    }

    private suspend fun searchByUsername(username: String) {
        val usernames = searchInteractor.searchByName(username)
        if (usernames.isEmpty()) {
            showNotFound()
            return
        }

        view?.showMessage(R.string.search_found)
        view?.showSearchResult(usernames)
    }

    private suspend fun searchBySolAddress(address: String) {
        val publicKey = try {
            PublicKey(address)
        } catch (e: Throwable) {
            Timber.i(e)
            showNotFound()
            return
        }

        val result = searchInteractor.searchByAddress(publicKey.toBase58().toBase58Instance())
        view?.showMessage(R.string.search_found)
        view?.showSearchResult(result)
    }

    private fun showEmptyState() {
        view?.showMessage(null)
        view?.showSearchResult(emptyList())
    }

    private fun showNotFound() {
        view?.showMessage(null)
        view?.showSearchResult(emptyList())
        view?.showNotFound()
    }
}
