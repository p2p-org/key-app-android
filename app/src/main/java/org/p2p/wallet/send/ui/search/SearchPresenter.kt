package org.p2p.wallet.send.ui.search

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SearchTarget
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DELAY_IN_MS = 250L

class SearchPresenter(
    private val usernames: List<SearchResult>?,
    private val searchInteractor: SearchInteractor,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle
) : BasePresenter<SearchContract.View>(), SearchContract.Presenter {

    private var searchJob: Job? = null

    override fun loadInitialData() {
        val data = usernames
        if (data.isNullOrEmpty()) return
        val message = if (data.size > 1) {
            R.string.send_multiple_accounts
        } else {
            R.string.send_account_found
        }

        val value = (data.first() as SearchResult.Full).username
        view?.showMessage(message)
        view?.showSearchResult(data)
        view?.showSearchValue(value)
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
                Timber.w("Cancelled search target validation: ${target.value}")
            } catch (e: Throwable) {
                Timber.e(e, "Error searching target")
                view?.showErrorMessage(e)
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

    private suspend fun validateAndSearch(target: SearchTarget) {
        when (target.validation) {
            SearchTarget.Validation.USERNAME -> searchByUsername(target.trimmedUsername)
            SearchTarget.Validation.SOL_ADDRESS -> searchBySolAddress(target.value)
            SearchTarget.Validation.BTC_ADDRESS -> showBtcAddress(target.value)
            SearchTarget.Validation.EMPTY -> showEmptyState()
            SearchTarget.Validation.INVALID -> showNotFound()
        }
    }

    private suspend fun searchByUsername(username: String) {
        val usernames = searchInteractor.searchByName(username)
        if (usernames.isEmpty()) {
            showNotFound()
            return
        }

        val message = if (usernames.size > 1) {
            R.string.send_multiple_accounts
        } else {
            R.string.send_account_found
        }

        view?.showMessage(message)
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
        view?.showMessage(R.string.send_account_found)
        view?.showSearchResult(result)
    }

    private fun showBtcAddress(address: String) {
        val searchResult = SearchResult.AddressOnly(AddressState(address, NetworkType.BITCOIN))
        val resultList = listOf(searchResult)
        view?.showMessage(null)
        view?.showSearchResult(resultList)
    }

    private fun showEmptyState() {
        view?.showMessage(null)
        view?.showSearchResult(emptyList())
    }

    private fun showNotFound() {
        view?.showMessage(R.string.send_no_address)
        view?.showSearchResult(emptyList())
    }
}
