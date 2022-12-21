package org.p2p.wallet.newsend.ui.search

import androidx.annotation.StringRes
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

    private var lastQuery: String? = null
    private var searchJob: Job? = null
    private var lastResult: List<SearchResult> = emptyList()
    private var recentRecipients: List<SearchResult> = emptyList()

    override fun loadInitialData() {
        launch {
            // TODO make it more optimized
            val finalSearchResult = lastResult.takeIf { lastResult.isNotEmpty() } ?: usernames
            if (finalSearchResult.isNullOrEmpty()) {
                recentRecipients = userInteractor.getRecipients()
                if (recentRecipients.isEmpty()) {
                    view?.showEmptyState(isEmpty = true)
                } else {
                    setResult(recentRecipients, R.string.search_recently)
                }
            } else {
                val searchedItem = finalSearchResult.first()
                val value = (searchedItem as? SearchResult.UsernameFound)?.username
                    ?: searchedItem.addressState.address
                view?.showSearchValue(lastQuery ?: value)
                setResult(finalSearchResult)
            }
        }
    }

    override fun search(newQuery: String) {
        if (lastQuery != newQuery) {
            lastQuery = newQuery
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
            if (result is SearchResult.AddressOnly) {
                finalResult = result.copyWithBalance(
                    userInteractor.getBalance(
                        result.addressState.address.toBase58Instance()
                    )
                )
                preselectedToken = result.sourceToken ?: initialToken
            } else {
                finalResult = result
                preselectedToken = initialToken
            }
            view?.submitSearchResult(finalResult, preselectedToken)
        }
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

    private fun setResult(
        result: List<SearchResult>,
        @StringRes messageRes: Int = R.string.search_found
    ) {
        lastResult = result
        view?.apply {
            showMessage(messageRes)
            showSearchResult(result)
            val invalidResult = result.findInstance<SearchResult.InvalidResult>()
            setBuyReceiveButtonsVisibility(invalidResult?.canReceiveAndBuy == true)
            setListBackgroundVisibility(invalidResult == null)
        }
    }

    private fun showEmptyState() {
        if (recentRecipients.isEmpty()) {
            view?.showEmptyState(isEmpty = true)
        } else {
            setResult(recentRecipients, R.string.search_recently)
        }
    }

    private fun showNotFound() {
        view?.showMessage(null)
        view?.showSearchResult(emptyList())
        view?.showNotFound()
    }
}
