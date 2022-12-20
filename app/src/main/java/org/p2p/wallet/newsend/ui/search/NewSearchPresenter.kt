package org.p2p.wallet.newsend.ui.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SearchTarget
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.findInstance
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

private const val DELAY_IN_MS = 250L

class NewSearchPresenter(
    private val usernames: List<SearchResult>?,
    private val initialToken: Token.Active?,
    private val searchInteractor: SearchInteractor,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle,
    private val userInteractor: UserInteractor,
) : BasePresenter<NewSearchContract.View>(), NewSearchContract.Presenter {

    private var searchJob: Job? = null
    private var lastResult: List<SearchResult> = emptyList()

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
        checkPreselectedTokenAndSubmitResult(result)
    }

    override fun onScanClicked() {
        view?.showScanner()
    }

    override fun onContinueClicked() {
        lastResult.findInstance<SearchResult.EmptyBalance>()?.let {
            checkPreselectedTokenAndSubmitResult(it)
        }
    }

    fun checkPreselectedTokenAndSubmitResult(result: SearchResult) {
        val preselectedToken = if (result is SearchResult.AddressOnly) {
            // in case if user inserts direct token address
            result.sourceToken ?: initialToken
        } else initialToken
        view?.submitSearchResult(result, preselectedToken)
    }

    override fun onBuyClicked() {
        launch {
            val tokenForBuying = userInteractor.getTokensForBuy().firstOrNull()
            if (tokenForBuying == null) {
                Timber.e("Unable to find a token for buying")
                return@launch
            }
            view?.showBuyScreen(tokenForBuying)
        }
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

        val result = searchInteractor.searchByAddress(
            publicKey.toBase58().toBase58Instance(),
            initialToken
        )
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
