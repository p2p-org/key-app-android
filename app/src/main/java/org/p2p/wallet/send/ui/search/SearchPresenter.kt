package org.p2p.wallet.send.ui.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.Target
import timber.log.Timber

private const val DELAY_IN_MS = 250L

class SearchPresenter(
    private val usernames: List<SearchResult>?,
    private val searchInteractor: SearchInteractor
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
        view?.showResult(data)
        view?.showSearchValue(value)
    }

    override fun search(target: Target) {
        searchJob?.cancel()
        searchJob = launch {
            try {
                delay(DELAY_IN_MS)
                view?.showLoading(true)
                validate(target)
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
        if (searchInteractor.isOwnPublicKey(result.address)) view?.showMessage(R.string.main_send_to_yourself_error)
        else view?.submitSearchResult(result)
    }

    private suspend fun validate(target: Target) {
        when (target.validation) {
            Target.Validation.USERNAME -> searchByUsername(target.trimmedUsername)
            Target.Validation.ADDRESS -> searchByAddress(target.value)
            Target.Validation.EMPTY -> showEmptyState()
            Target.Validation.INVALID -> showNotFound()
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
        view?.showResult(usernames)
    }

    private suspend fun searchByAddress(address: String) {
        val publicKey = try {
            PublicKey(address)
        } catch (e: Throwable) {
            showNotFound()
            return
        }

        val result = searchInteractor.searchByAddress(publicKey.toBase58())
        view?.showMessage(R.string.send_account_found)
        view?.showResult(result)
    }

    private fun showEmptyState() {
        view?.showMessage(null)
        view?.showResult(emptyList())
    }

    private fun showNotFound() {
        view?.showMessage(R.string.send_no_address)
        view?.showResult(emptyList())
    }
}
