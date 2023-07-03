package org.p2p.wallet.home.ui.main

import androidx.lifecycle.LifecycleOwner
import android.content.Context
import android.content.res.Resources
import timber.log.Timber
import java.math.BigDecimal
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.core.utils.Constants.USDT_SYMBOL
import org.p2p.core.utils.Constants.WETH_SYMBOL
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.HomePresenterMapper
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.main.models.HomeScreenViewState
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.intercom.IntercomDeeplinkManager
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.solana.SolanaNetworkObserver
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.updates.SocketState
import org.p2p.wallet.updates.SubscriptionUpdatesManager
import org.p2p.wallet.updates.SubscriptionUpdatesStateObserver
import org.p2p.wallet.updates.subscribe.SubscriptionUpdateSubscriber
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.user.worker.PendingTransactionMergeWorker
import org.p2p.wallet.utils.ellipsizeAddress
import org.p2p.wallet.utils.toPublicKey
import org.p2p.wallet.utils.unsafeLazy

val POPULAR_TOKENS_SYMBOLS: Set<String> = setOf(USDC_SYMBOL, SOL_SYMBOL, WETH_SYMBOL, USDT_SYMBOL)
// TODO add fetching prices for this tokens
// val POPULAR_TOKENS_COINGECKO_IDS: List<TokenCoinGeckoId> = setOf(
//    SOL_COINGECKO_ID,
//    USDT_COINGECKO_ID,
//    WETH_COINGECKO_ID,
//    USDC_COINGECKO_ID
// ).map(::TokenCoinGeckoId)
val TOKEN_SYMBOLS_VALID_FOR_BUY: List<String> = listOf(USDC_SYMBOL, SOL_SYMBOL)

class HomePresenter(
    // interactors
    private val homeInteractor: HomeInteractor,
    private val userInteractor: UserInteractor,
    // other
    private val tokensPolling: UserTokensPolling,
    private val networkObserver: SolanaNetworkObserver,
    // managers
    private val updatesManager: SubscriptionUpdatesManager,
    private val environmentManager: NetworkEnvironmentManager,
    private val deeplinksManager: AppDeeplinksManager,
    private val connectionManager: ConnectionManager,
    private val transactionManager: TransactionManager,
    private val updateSubscribers: List<SubscriptionUpdateSubscriber>,
    private val intercomDeeplinkManager: IntercomDeeplinkManager,
    // mappers
    private val homeMapper: HomePresenterMapper,
    // FT
    private val newBuyFeatureToggle: NewBuyFeatureToggle,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    // analytics
    private val analytics: HomeAnalytics,
    private val userTokensInteractor: UserTokensInteractor,
    seedPhraseProvider: SeedPhraseProvider,
    tokenKeyProvider: TokenKeyProvider,
    bridgeFeatureToggle: EthAddressEnabledFeatureToggle,
    context: Context
) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {

    private val resources: Resources = context.resources

    private var username: Username? = null

    private var state = HomeScreenViewState(areZerosHidden = homeInteractor.areZerosHidden())
    private val buttonsStateFlow = MutableStateFlow<List<ActionButton>>(emptyList())

    // use flow since it's the only way we can show progress before view is attached
    private val refreshingFlow = MutableStateFlow(true)

    private val userPublicKey: String by unsafeLazy { tokenKeyProvider.publicKey }
    private var homeStateSubscribed = false
    private var loadSolTokensJob: Job? = null

    private val deeplinkHandler by unsafeLazy {
        HomePresenterDeeplinkHandler(
            coroutineScope = this,
            presenter = this,
            view = view,
            state = state,
            userInteractor = userInteractor
        )
    }

    init {
        val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase
        if (userSeedPhrase.isNotEmpty() && bridgeFeatureToggle.isFeatureEnabled) {
            homeInteractor.setupEthereumKit(userSeedPhrase = userSeedPhrase)
            PendingTransactionMergeWorker.scheduleWorker(context)
        } else {
            Timber.w("ETH is not initialized, no seed phrase or disabled")
        }
        launch {
            awaitAll(
                async { networkObserver.start() },
                async { homeInteractor.loadInitialAppData() }
            )

            // save the job to prevent do the same job twice in observeInternetConnection
            loadSolTokensJob = loadSolTokensAndRates()
            loadSolTokensJob?.join()
            loadSolTokensJob = null

            attachToPollingTokens()
        }

        updatesManager.addUpdatesStateObserver(object : SubscriptionUpdatesStateObserver {
            override fun onUpdatesStateChanged(state: SocketState) {
                if (state == SocketState.CONNECTED) {
                    updateSubscribers.forEach {
                        it.subscribe()
                    }
                }
            }
        })
    }

    override fun attach(view: HomeContract.View) {
        super.attach(view)
        if (state.tokens.isNotEmpty() || state.ethTokens.isNotEmpty()) {
            handleHomeStateChanged(state.tokens, state.ethTokens)
        }
        observeRefreshingStatus()
        observeInternetConnection()
        observeActionButtonState()
        handleDeeplinks()
        launch {
            if (loadSolTokensJob == null) {
                attachToPollingTokens()
            }
        }
    }

    override fun refreshTokens() {
        launchInternetAware(connectionManager) {
            try {
                showRefreshing(isRefreshing = true)
                tokensPolling.refreshTokens()
                initializeActionButtons(isRefreshing = true)
            } catch (cancelled: CancellationException) {
                Timber.i("Loading tokens job cancelled")
            } catch (error: Throwable) {
                Timber.e(error, "Error refreshing user tokens")
                view?.showErrorMessage(error)
            } finally {
                showRefreshing(isRefreshing = false)
            }
        }
    }

    private suspend fun attachToPollingTokens() {
        if (homeStateSubscribed) return
        homeStateSubscribed = true

        tokensPolling.shareTokenPollFlowIn(this)
            .filterNotNull()
            .combine(homeInteractor.getUserStatusBannerFlow()) { homeState, strigaBanner ->
                homeState to strigaBanner
            }
            .onCompletion { homeStateSubscribed = false }
            .collect { (homeState, strigaBanner) ->
                logHomeStateChanged(homeState)

                state = state.copy(
                    tokens = homeState.solTokens,
                    ethTokens = homeState.ethTokens,
                    strigaKycStatusBanner = strigaBanner,
                    strigaClaimableTokens = homeState.claimableTokens
                )
                initializeActionButtons()
                handleHomeStateChanged(homeState.solTokens, homeState.ethTokens)
                showRefreshing(homeState.isRefreshing)
            }
    }

    private fun logHomeStateChanged(homeState: UserTokensPollState) {
        val solTokensLog = homeState.solTokens
            .joinToString { "${it.tokenSymbol}(${it.total.formatToken()}; ${it.totalInUsd?.formatFiat()})" }
        Timber.d("Home state solTokens: $solTokensLog")
    }

    private fun observeActionButtonState() {
        launch {
            buttonsStateFlow.collect { buttons ->
                view?.showActionButtons(buttons)
            }
        }
    }

    private fun observeRefreshingStatus() {
        refreshingFlow.onEach {
            view?.showRefreshing(it)
        }
            .launchIn(this)
    }

    private fun observeInternetConnection() {
        launch {
            connectionManager.connectionStatus.collect { hasConnection ->
                if (hasConnection) {
                    if (!updatesManager.isStarted()) {
                        updatesManager.restart()
                    }

                    // we should reload tokens if we have reconnected internet
                    // but don't load if this job is already running in constructor
                    if (loadSolTokensJob == null) {
                        loadSolTokensJob = loadSolTokensAndRates()
                        // join and set null to be able to relaunch this job after next reconnections
                        loadSolTokensJob?.join()
                        loadSolTokensJob = null
                        attachToPollingTokens()
                    }
                } else {
                    if (updatesManager.isStarted()) {
                        updatesManager.stop()
                    }
                }
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        intercomDeeplinkManager.proceedDeeplinkIfExists()
    }

    override fun load() {
        showUserAddressAndUsername()

        updatesManager.start()

        val userId = username?.value ?: userPublicKey
        IntercomService.signIn(userId)

        environmentManager.addEnvironmentListener(this::class) { refreshTokens() }
    }

    override fun onClaimClicked(canBeClaimed: Boolean, token: Token.Eth) {
        launch {
            analytics.logClaimButtonClicked()
            if (canBeClaimed) {
                view?.showTokenClaim(token)
            } else {
                val latestActiveBundleId = token.latestActiveBundleId ?: return@launch
                val bridgeBundle = homeInteractor.getClaimBundleById(latestActiveBundleId) ?: return@launch
                val claimDetails = homeMapper.mapToClaimDetails(
                    bridgeBundle = bridgeBundle,
                    minAmountForFreeFee = homeInteractor.getClaimMinAmountForFreeFee(),
                )
                val progressDetails = homeMapper.mapShowProgressForClaim(
                    amountToClaim = bridgeBundle.resultAmount.amountInToken,
                    iconUrl = token.iconUrl.orEmpty(),
                    claimDetails = claimDetails
                )
                transactionManager.emitTransactionState(
                    latestActiveBundleId,
                    TransactionState.ClaimProgress(latestActiveBundleId)
                )
                view?.showProgressDialog(
                    bundleId = bridgeBundle.bundleId,
                    progressDetails = progressDetails
                )
            }
        }
    }

    override fun onBannerClicked(bannerTitleId: Int) {
        val statusFromKycBanner = homeMapper.getKycStatusBannerFromTitle(bannerTitleId)
        when {
            statusFromKycBanner == StrigaKycStatusBanner.PENDING -> {
                view?.showKycPendingDialog()
            }
            statusFromKycBanner != null -> {
                view?.navigateToKycStatus(statusFromKycBanner)
            }
            else -> {
                view?.showTopupWalletDialog()
            }
        }
    }

    override fun onBannerCloseClicked(bannerTitleId: Int) = Unit

    /**
     * Don't split this method, as it could lead to one more data race since rates are loading asynchronously
     */
    private fun loadSolTokensAndRates(): Job = launch {
        showRefreshing(true)
        try {
            // this job also depends on the internet
            homeInteractor.loadAllTokensDataIfEmpty()
            val tokens = homeInteractor.loadUserTokensAndUpdateLocal(userPublicKey.toPublicKey())
            userTokensInteractor.loadUserRates(tokens)
        } catch (e: CancellationException) {
            Timber.d("Loading sol tokens job cancelled")
        } catch (e: UnknownHostException) {
            Timber.d("Cannot load sol tokens: no internet")
        } catch (t: Throwable) {
            Timber.e(t, "Error on loading sol tokens")
        } finally {
            showRefreshing(false)
        }
    }

    private fun handleDeeplinks() {
        launchSupervisor {
            deeplinksManager.subscribeOnDeeplinks(
                setOf(
                    DeeplinkTarget.BUY,
                    DeeplinkTarget.SEND,
                    DeeplinkTarget.SWAP,
                    DeeplinkTarget.CASH_OUT
                )
            ).collect(deeplinkHandler::handle)
        }
    }

    private suspend fun initializeActionButtons(isRefreshing: Boolean = false) {
        if (!isRefreshing && buttonsStateFlow.value.isNotEmpty()) {
            return
        }
        val isSellFeatureToggleEnabled = sellEnabledFeatureToggle.isFeatureEnabled
        val isSellAvailable = homeInteractor.isSellFeatureAvailable()

        val buttons = mutableListOf(ActionButton.TOP_UP_BUTTON)
        if (isSellAvailable) {
            buttons += ActionButton.SELL_BUTTON
        }

        buttons += ActionButton.SEND_BUTTON

        if (!isSellFeatureToggleEnabled) {
            buttons += ActionButton.SWAP_BUTTON
        }

        buttonsStateFlow.emit(buttons)
    }

    private fun showUserAddressAndUsername() {
        this.username = homeInteractor.getUsername()
        val userAddress = username?.fullUsername ?: userPublicKey.ellipsizeAddress()
        view?.showUserAddress(userAddress)
        state = state.copy(
            username = username,
            visibilityState = VisibilityState.create(homeInteractor.getHiddenTokensVisibility())
        )
    }

    override fun onAddressClicked() {
        view?.showAddressCopied(username?.fullUsername ?: userPublicKey)
    }

    override fun onBuyClicked() {
        launch {
            val tokensForBuy = homeInteractor.getTokensForBuy()
            if (tokensForBuy.isEmpty()) return@launch

            if (newBuyFeatureToggle.isFeatureEnabled) {
                // this cannot be empty
                view?.navigateToBuyScreen(tokensForBuy.first())
            } else {
                view?.showTokensForBuy(tokensForBuy)
            }
        }
    }

    override fun onSellClicked() {
        analytics.logSellSubmitClicked()
        view?.showCashOut()
    }

    override fun onSwapClicked() {
        analytics.logSwapActionButtonClicked()
        view?.showSwap()
    }

    override fun onTopupClicked() {
        analytics.logTopupHomeBarClicked()
        view?.showTopup()
    }

    override fun onBuyTokenClicked(token: Token) {
        if (token.tokenSymbol !in TOKEN_SYMBOLS_VALID_FOR_BUY) {
            view?.showBuyInfoScreen(token)
        } else {
            onBuyToken(token)
        }
    }

    override fun onInfoBuyTokenClicked(token: Token) {
        onBuyToken(token)
    }

    private fun onBuyToken(token: Token) {
        launch {
            val tokenToBuy = if (token.isSOL || token.isUSDC) {
                token
            } else {
                homeInteractor.getSingleTokenForBuy() ?: return@launch
            }
            view?.navigateToBuyScreen(tokenToBuy)
        }
    }

    override fun onSendClicked(clickSource: SearchOpenedFromScreen) {
        analytics.logSendActionButtonClicked()
        launch {
            val isEmptyAccount = state.tokens.all { it.isZero }
            if (isEmptyAccount) {
                // this cannot be empty
                val validTokenToBuy = homeInteractor.getSingleTokenForBuy() ?: return@launch
                view?.showSendNoTokens(validTokenToBuy)
            } else {
                view?.showNewSendScreen(clickSource)
            }
        }
    }

    private fun handleHomeStateChanged(
        userTokens: List<Token.Active>,
        ethTokens: List<Token.Eth>,
    ) {
        Timber.d("local tokens change arrived")
        state = state.copy(
            tokens = userTokens,
            ethTokens = ethTokens,
            username = homeInteractor.getUsername(),
        )
        val isAccountEmpty = userTokens.all(Token.Active::isZero) && ethTokens.isEmpty()
        when {
            isAccountEmpty -> {
                view?.showEmptyState(isEmpty = true)
                handleEmptyAccount()
            }

            (userTokens.isNotEmpty() || ethTokens.isNotEmpty()) -> {
                view?.showEmptyState(isEmpty = false)
                showTokensAndBalance()
            }
        }
    }

    private fun handleEmptyAccount() {
        val tokensForBuy =
            homeInteractor.findMultipleTokenData(POPULAR_TOKENS_SYMBOLS.toList())
                .sortedBy { tokenToBuy -> POPULAR_TOKENS_SYMBOLS.indexOf(tokenToBuy.tokenSymbol) }

        val strigaBigBanner = state.strigaKycStatusBanner
            ?.let(homeMapper::mapToBigBanner)
            ?: getDefaultBanner()

        val emptyDataList = buildList {
            this += strigaBigBanner
            this += resources.getString(R.string.main_popular_tokens_header)
            addAll(tokensForBuy)
        }
        view?.showEmptyViewData(emptyDataList)
        logBalance(BigDecimal.ZERO)

        view?.showBalance(homeMapper.mapBalance(BigDecimal.ZERO))
    }

    override fun toggleTokenVisibility(token: Token.Active) {
        launch {
            val handleDefaultVisibility = { token: Token.Active ->
                if (homeInteractor.areZerosHidden() && token.isZero) {
                    TokenVisibility.SHOWN
                } else {
                    TokenVisibility.HIDDEN
                }
            }
            val newVisibility = when (token.visibility) {
                TokenVisibility.SHOWN -> TokenVisibility.HIDDEN
                TokenVisibility.HIDDEN -> TokenVisibility.SHOWN
                TokenVisibility.DEFAULT -> handleDefaultVisibility(token)
            }

            homeInteractor.setTokenHidden(
                mintAddress = token.mintAddress,
                visibility = newVisibility.stringValue
            )

            val updatedTokens = homeInteractor.getUserTokens()
            handleHomeStateChanged(updatedTokens, state.ethTokens)
        }
    }

    override fun toggleTokenVisibilityState() {
        state = state.run { copy(visibilityState = visibilityState.toggle()) }
        homeInteractor.setHiddenTokensVisibility(state.visibilityState.isVisible)

        showTokensAndBalance()
    }

    override fun clearTokensCache() {
        state = state.copy(tokens = emptyList())
    }

    private fun getDefaultBanner(): HomeBannerItem {
        return HomeBannerItem(
            titleTextId = R.string.main_banner_title,
            subtitleTextId = R.string.main_banner_subtitle,
            buttonTextId = R.string.main_banner_button,
            drawableRes = R.drawable.ic_main_banner,
            backgroundColorRes = R.color.bannerBackgroundColor
        )
    }

    private fun showTokensAndBalance() {
        launchInternetAware(connectionManager) {
            val balance = getUserBalance()

            if (balance != null) {
                view?.showBalance(homeMapper.mapBalance(balance))
            } else {
                view?.showBalance(null)
            }

            logBalance(balance)

            /* Mapping elements according to visibility settings */
            val areZerosHidden = homeInteractor.areZerosHidden()
            val mappedTokens: List<HomeElementItem> = homeMapper.mapToItems(
                tokens = state.tokens,
                ethereumTokens = state.ethTokens,
                visibilityState = state.visibilityState,
                strigaClaimableTokens = state.strigaClaimableTokens,
                isZerosHidden = areZerosHidden,
            )

            analytics.logClaimAvailable(ethTokens = state.ethTokens)
            val strigaBanner = state.strigaKycStatusBanner?.let(homeMapper::mapToHomeBanner)
            val homeToken = buildList {
                if (strigaBanner != null) {
                    this += HomeElementItem.Banner(strigaBanner)
                }
                addAll(mappedTokens)
            }

            view?.showTokens(homeToken, areZerosHidden)
        }
    }

    private fun logBalance(balance: BigDecimal?) {
        val hasPositiveBalance = balance != null && balance.isMoreThan(BigDecimal.ZERO)
        analytics.logUserHasPositiveBalanceProperty(hasPositiveBalance)
        analytics.logUserAggregateBalanceProperty(balance.orZero())
    }

    private fun getUserBalance(): BigDecimal? {
        val tokens = state.tokens

        if (tokens.none { it.totalInUsd != null }) return null

        return tokens
            .mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()
    }

    override fun onProfileClick() {
        if (homeInteractor.isUsernameExist()) {
            view?.navigateToProfile()
        } else {
            view?.navigateToReserveUsername()
        }
    }

    override fun updateTokensIfNeeded() {
        if (state.areZerosHidden != homeInteractor.areZerosHidden()) {
            refreshTokens()
            state = state.copy(areZerosHidden = homeInteractor.areZerosHidden())
        }
    }

    override fun detach() {
        environmentManager.removeEnvironmentListener(this::class)
        super.detach()
    }

    private fun showRefreshing(isRefreshing: Boolean) = refreshingFlow.tryEmit(isRefreshing)
}
