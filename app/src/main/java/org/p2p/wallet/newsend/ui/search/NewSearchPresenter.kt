package org.p2p.wallet.newsend.ui.search

import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SendViaLinkFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.newsend.interactor.SearchInteractor
import org.p2p.wallet.newsend.model.NetworkType
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.SearchState
import org.p2p.wallet.newsend.model.SearchTarget
import org.p2p.wallet.svl.model.SvlWidgetState
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toPublicKey

private const val DELAY_IN_MS = 250L

class NewSearchPresenter(
    private val initialToken: Token.Active?,
    private val searchInteractor: SearchInteractor,
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle,
    private val userInteractor: UserInteractor,
    private val newSendAnalytics: NewSendAnalytics,
    private val sendViaLinkFeatureToggle: SendViaLinkFeatureToggle,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor
) : BasePresenter<NewSearchContract.View>(), NewSearchContract.Presenter {

    private var state = SearchState()
    private var searchJob: Job? = null

    override fun attach(view: NewSearchContract.View) {
        super.attach(view)
        newSendAnalytics.logNewSearchScreenOpened()

        loadFeeLimits()

        if (state.recentRecipients == null) {
            loadRecentRecipients()
        } else {
            renderCurrentState()
        }
    }

    private fun loadRecentRecipients() {
        launch {
            val recipients = userInteractor.getRecipients()
            state.updateRecipients(recipients)

            renderCurrentState()
        }
    }

    private fun renderCurrentState() {
        when (val currentState = state.state) {
            is SearchState.State.UsersFound -> {
                showSendViaLinkContainer(isVisible = false)
                view?.showUsers(currentState.users)
                view?.showUsersMessage(R.string.search_found)
                view?.updateSearchInput(currentState.query, submit = false)
                view?.showBackgroundVisible(isVisible = true)
            }
            is SearchState.State.UsersNotFound -> {
                showSendViaLinkContainer(isVisible = false)
                view?.showNotFound()
                view?.showUsersMessage(null)
                view?.updateSearchInput(currentState.query, submit = false)
                view?.showBackgroundVisible(isVisible = true)
            }
            is SearchState.State.ShowInvalidAddresses -> {
                showSendViaLinkContainer(isVisible = false)
                view?.showUsers(currentState.users)
                view?.showUsersMessage(R.string.search_found)
                view?.showBackgroundVisible(isVisible = false)
            }
            is SearchState.State.ShowRecipients -> {
                showSendViaLinkContainer(isVisible = true)
                view?.showUsers(currentState.recipients)
                view?.showUsersMessage(R.string.search_recently)
                view?.showBackgroundVisible(isVisible = true)
            }
            is SearchState.State.ShowEmptyState -> {
                showSendViaLinkContainer(isVisible = true)
                view?.showEmptyState(isEmpty = true)
                view?.showUsersMessage(null)
                view?.clearUsers()
                view?.showBackgroundVisible(isVisible = true)
            }
        }
    }

    override fun search(newQuery: String) {
        // when screen is restored, the searchView triggers the queryChange automatically
        // we don't need to make a new request, since we already restored the state in attach
        if (state.query == newQuery) {
            return
        }

        if (newQuery.isBlank()) {
            searchJob?.cancel()
            state.clear()
            renderCurrentState()
            return
        }

        state.updateSearchResult(newQuery, emptyList())

        val target = SearchTarget(
            value = newQuery,
            keyAppDomainIfUsername = usernameDomainFeatureToggle.value,
            isEthAddressEnabled = ethAddressEnabledFeatureToggle.isFeatureEnabled
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
                view?.showLoading(isLoading = false)
            }
        }
    }

    override fun onSearchResultClick(result: SearchResult) {
        checkPreselectedTokenAndSubmitResult(result)
    }

    override fun onScanClicked() {
        view?.showScanner()
    }

    private fun checkPreselectedTokenAndSubmitResult(result: SearchResult) {
        launch {
            val finalResult: SearchResult
            val preselectedToken: Token.Active?
            if (result is SearchResult.AddressFound && result.networkType == NetworkType.SOLANA) {
                val balance = userInteractor.getBalance(result.addressState.address.toPublicKey())
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

    private fun showSendViaLinkContainer(isVisible: Boolean) {
        if (!sendViaLinkFeatureToggle.isFeatureEnabled) {
            view?.showSendViaLink(isVisible = false)
            return
        }

        view?.showSendViaLink(isVisible = isVisible)
    }

    private suspend fun validateAndSearch(target: SearchTarget) {
        when (target.validation) {
            SearchTarget.Validation.USERNAME -> searchByUsername(target.trimmedUsername)
            SearchTarget.Validation.SOLANA_TYPE_ADDRESS -> searchBySolAddress(target.value)
            SearchTarget.Validation.ETHEREUM_TYPE_ADDRESS -> searchByEthereumAddress(target.value)
            SearchTarget.Validation.EMPTY -> renderCurrentState()
            else -> showNotFound()
        }
    }

    private suspend fun validateOnlyAddress(target: SearchTarget) {
        when (target.validation) {
            SearchTarget.Validation.SOLANA_TYPE_ADDRESS -> searchBySolAddress(target.value)
            SearchTarget.Validation.ETHEREUM_TYPE_ADDRESS -> searchByEthereumAddress(target.value)
            else -> {
                view?.showErrorState()
                view?.showUsersMessage(textRes = null)
            }
        }
    }

    private suspend fun searchByUsername(username: String) {
        val usernames = searchInteractor.searchByName(username)
        state.updateSearchResult(username, usernames)
        renderCurrentState()
    }

    private suspend fun searchBySolAddress(address: String) {
        val publicKey = try {
            PublicKey(address)
        } catch (e: Throwable) {
            Timber.i(e)
            state.updateSearchResult(address, emptyList())
            renderCurrentState()
            return
        }

        val newAddresses = searchInteractor.searchByAddress(publicKey, initialToken)
        state.updateSearchResult(address, listOf(newAddresses))
        renderCurrentState()
    }

    private suspend fun searchByEthereumAddress(address: String) {
        val newAddresses = searchInteractor.searchByEthAddress(EthAddress(address))
        state.updateSearchResult(address, listOf(newAddresses))
        renderCurrentState()
    }

    private fun showNotFound() {
        view?.showUsersMessage(null)
        view?.clearUsers()
        view?.showNotFound()
    }

    private fun loadFeeLimits() {
        launch {
            try {
                view?.showLoading(isLoading = true)
                val feeLimits = feeRelayerAccountInteractor.getFreeTransactionFeeLimit(useCache = false)
                val isSendViaLinkAvailable = feeLimits.isSendViaLinkAllowed()
                val state = if (true) SvlWidgetState.ENABLED else SvlWidgetState.DISABLED
                view?.updateLinkWidgetState(state)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading free transaction limits")
                view?.updateLinkWidgetState(SvlWidgetState.DISABLED)
            } finally {
                view?.showLoading(isLoading = false)
            }
        }
    }

    private fun logRecipientSelected(recipient: SearchResult) {
        newSendAnalytics.logRecipientSelected(recipient, state.foundResult)
    }
}
