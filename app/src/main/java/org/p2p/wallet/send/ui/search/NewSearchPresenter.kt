package org.p2p.wallet.send.ui.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.core.token.Token
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SearchTarget
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.core.utils.Constants
import org.p2p.wallet.utils.findInstance
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

private const val DELAY_IN_MS = 250L

class NewSearchPresenter(
    private val usernames: List<SearchResult>?,
    private val searchInteractor: SearchInteractor,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle,
    private val userInteractor: UserInteractor,
) : BasePresenter<NewSearchContract.View>(), NewSearchContract.Presenter {

    private var searchJob: Job? = null
    private var lastResult: List<SearchResult> = emptyList()
    private var usdcTokenForBuy: Token? = null

    init {
        launch {
            usdcTokenForBuy = userInteractor.getTokensForBuy(listOf(Constants.USDC_SYMBOL)).firstOrNull()
        }
    }

    override fun loadInitialData() {
        val data = usernames
        if (data.isNullOrEmpty()) {
            // TODO PWN-6077 check latest recipients and show if exists
            view?.showEmptyState(isEmpty = true)
        } else {
            val value = (data.first() as SearchResult.UsernameFound).username
            view?.showSearchValue(value)
            setResult(data)
        }
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
        view?.submitSearchResult(result)
    }

    override fun onScanClicked() {
        view?.showScanner()
    }

    override fun onContinueClicked() {
        lastResult.findInstance<SearchResult.EmptyBalance>()?.let {
            view?.submitSearchResult(it)
        }
    }

    override fun onBuyClicked() {
        usdcTokenForBuy?.let {
            view?.showBuyScreen(it)
        } ?: Timber.i("Unable to find USDC TokenForBuy!")
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

        setResult(usernames)
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
        setResult(result)
    }

    private fun setResult(result: List<SearchResult>) {
        lastResult = result
        view?.apply {
            showMessage(R.string.search_found)
            showSearchResult(result)
            setContinueButtonVisibility(result.findInstance<SearchResult.EmptyBalance>() != null)
            val invalidResult = result.findInstance<SearchResult.InvalidResult>()
            setBuyReceiveButtonsVisibility(invalidResult?.canReceiveAndBuy == true)
            setListBackgroundVisibility(invalidResult == null)
        }
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
