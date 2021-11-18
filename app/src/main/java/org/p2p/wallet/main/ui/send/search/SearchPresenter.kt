package org.p2p.wallet.main.ui.send.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.main.interactor.SearchInteractor
import org.p2p.wallet.main.model.Target
import timber.log.Timber

private const val DELAY_IN_MS = 250L

class SearchPresenter(
    private val searchInteractor: SearchInteractor
) : BasePresenter<SearchContract.View>(), SearchContract.Presenter {

    private var searchJob: Job? = null

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

    private suspend fun validate(target: Target) {
        when (target.validation) {
            Target.Validation.USERNAME -> searchByUsername(target.trimmedValue)
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
        val validatedAddress = try {
            PublicKey(address)
        } catch (e: Throwable) {
            showNotFound()
            null
        } ?: return

        val result = searchInteractor.searchByAddress(validatedAddress.toBase58())
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