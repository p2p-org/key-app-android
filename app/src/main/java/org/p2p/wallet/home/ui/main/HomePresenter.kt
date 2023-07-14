package org.p2p.wallet.home.ui.main

import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.HomePresenterMapper
import org.p2p.wallet.home.ui.main.models.HomeScreenViewState
import org.p2p.wallet.home.ui.main.striga.StrigaOnRampConfirmedHandler
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.intercom.IntercomDeeplinkManager
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.updates.SubscriptionUpdatesManager
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.utils.unsafeLazy

val TOKEN_SYMBOLS_VALID_FOR_BUY: List<String> = listOf(USDC_SYMBOL, SOL_SYMBOL)

class HomePresenter(
    // interactors
    private val homeInteractor: HomeInteractor,
    private val updatesManager: SubscriptionUpdatesManager,
    private val environmentManager: NetworkEnvironmentManager,
    private val connectionManager: ConnectionManager,
    private val transactionManager: TransactionManager,
    private val intercomDeeplinkManager: IntercomDeeplinkManager,
    private val homeMapper: HomePresenterMapper,
    private val newBuyFeatureToggle: NewBuyFeatureToggle,
    private val strigaFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val userTokensInteractor: UserTokensInteractor,
    private val strigaInteractor: StrigaOnRampInteractor,
    private val onRampConfirmedHandler: StrigaOnRampConfirmedHandler,
    private val analytics: HomeAnalytics,
    tokenKeyProvider: TokenKeyProvider,
    private val tokenServiceCoordinator: TokenServiceCoordinator
) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {

    private var username: Username? = null

    private var state = HomeScreenViewState(areZerosHidden = homeInteractor.areZerosHidden())
    private val buttonsStateFlow = MutableStateFlow<List<ActionButton>>(emptyList())

    private val userPublicKey: String by unsafeLazy { tokenKeyProvider.publicKey }

    override fun attach(view: HomeContract.View) {
        super.attach(view)
        launch {
            if (state.tokens.isNotEmpty() || state.ethTokens.isNotEmpty()) {
                handleHomeStateChanged(state.tokens, state.ethTokens)
            }
        }
        observeActionButtonState()
        observeKycBanners()
        observeState()

        loadStrigaOnRampTokens()
    }

    override fun refreshTokens() {
        launchInternetAware(connectionManager) {
            try {
                tokenServiceCoordinator.refresh()
                initializeActionButtons(isRefreshing = true)
            } catch (cancelled: CancellationException) {
                Timber.i("Loading tokens job cancelled")
            } catch (error: Throwable) {
                Timber.e(error, "Error refreshing user tokens")
                view?.showErrorMessage(error)
            }
        }
    }

    private fun observeState() {
        tokenServiceCoordinator.observeUserTokens()
            .onEach { tokenState ->
                handleTokenState(tokenState)
            }
            .launchIn(this)
    }

    private fun handleTokenState(newState: UserTokensState) {
        view?.showRefreshing(isRefreshing = newState.isLoading())

        when (newState) {
            is UserTokensState.Idle -> Unit
            is UserTokensState.Loading -> Unit
            is UserTokensState.Refreshing -> Unit
            is UserTokensState.Error -> {
                view?.showErrorMessage(newState.cause)
            }
            is UserTokensState.Empty -> {
                view?.showEmptyState(isEmpty = true)
                handleEmptyAccount()
            }
            is UserTokensState.Loaded -> {
                view?.showEmptyState(isEmpty = false)
                initializeActionButtons()
                state = state.copy(
                    tokens = newState.solTokens,
                    ethTokens = newState.ethTokens
                )
                showTokensAndBalance()
            }
        }
    }

    private fun observeKycBanners() {
        if (!strigaFeatureToggle.isFeatureEnabled) {
            return
        }

        homeInteractor.getUserStatusBannerFlow()
            .onEach { banner ->
                state = state.copy(
                    strigaKycStatusBanner = banner
                )
            }
            .launchIn(this)
    }

    private fun observeActionButtonState() {
        launch {
            buttonsStateFlow.collect { buttons ->
                view?.showActionButtons(buttons)
            }
        }
    }

    private fun loadStrigaOnRampTokens() {
        launch {
            val claimTokens = strigaInteractor.getOnRampTokens().successOrNull().orEmpty()
            state = state.copy(
                strigaOnRampTokens = claimTokens
            )
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        intercomDeeplinkManager.proceedDeeplinkIfExists()
    }

    override fun load() {
        updatesManager.start()

        val userId = username?.value ?: userPublicKey
        IntercomService.signIn(userId)
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
                launch {
                    // hide banner if necessary
                    homeInteractor.hideStrigaUserStatusBanner(statusFromKycBanner)

                    if (statusFromKycBanner == StrigaKycStatusBanner.VERIFICATION_DONE) {
                        state = state.copy(isStrigaKycBannerLoading = true)
                        handleHomeStateChanged(state.tokens, state.ethTokens)

                        homeInteractor.loadDetailsForStrigaAccounts()
                            .onSuccess { view?.navigateToKycStatus(statusFromKycBanner) }
                            .onFailure { view?.showUiKitSnackBar(messageResId = R.string.error_general_message) }

                        state = state.copy(isStrigaKycBannerLoading = false)
                        handleHomeStateChanged(state.tokens, state.ethTokens)
                    } else {
                        view?.navigateToKycStatus(statusFromKycBanner)
                    }
                }
            }
            else -> {
                view?.showTopupWalletDialog()
            }
        }
    }

    override fun onStrigaOnRampTokenClicked(item: HomeElementItem.StrigaOnRampTokenItem) {
        launch {
            try {
                view?.showStrigaOnRampProgress(isOnRampInProgress = true, tokenMint = item.tokenMintAddress)
                val challengeId = homeInteractor.claimStrigaToken(item.amountAvailable, item.strigaToken).unwrap()
                view?.navigateToStrigaOnRampConfirmOtp(challengeId, item)
            } catch (e: Throwable) {
                Timber.e(e, "Error on claiming striga token")
                if (BuildConfig.DEBUG) {
                    view?.showErrorMessage(IllegalStateException("Striga claiming is not supported yet", e))
                } else {
                    view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                }
            } finally {
                view?.showStrigaOnRampProgress(isOnRampInProgress = false, tokenMint = item.tokenMintAddress)
            }
        }
    }

    override fun onOnRampConfirmed(
        challengeId: StrigaWithdrawalChallengeId,
        token: HomeElementItem.StrigaOnRampTokenItem
    ) {
        launch {
            onRampConfirmedHandler.handleConfirmed(token)
        }
    }

    private fun initializeActionButtons(isRefreshing: Boolean = false) {
        if (!isRefreshing && buttonsStateFlow.value.isNotEmpty()) {
            return
        }

        val buttons = mutableListOf(ActionButton.RECEIVE_BUTTON, ActionButton.SWAP_BUTTON)
        buttonsStateFlow.value = buttons
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

    override fun onReceiveClicked() {
        view?.showReceive()
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
        )
        val isAccountEmpty = userTokens.all(Token.Active::isZero) && ethTokens.isEmpty()
        when {
            isAccountEmpty -> {
                // fixme: fixed in line 145, this should be removed
//                view?.showEmptyState(isEmpty = true)
//                handleEmptyAccount()
            }

            (userTokens.isNotEmpty() || ethTokens.isNotEmpty()) -> {
                view?.showEmptyState(isEmpty = false)
                showTokensAndBalance()
            }
        }
    }

    private fun handleEmptyAccount() {
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

            val updatedTokens = userTokensInteractor.getUserTokens()
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
                strigaOnRampTokens = state.strigaOnRampTokens,
                isZerosHidden = areZerosHidden,
            )

            analytics.logClaimAvailable(ethTokens = state.ethTokens)
            val strigaBanner = state.strigaKycStatusBanner
                ?.let { homeMapper.mapToHomeBanner(isLoading = state.isStrigaKycBannerLoading, it) }
            val homeToken = buildList {
                if (strigaBanner != null) {
                    this += HomeElementItem.Banner(strigaBanner)
                }
                addAll(mappedTokens)
            }

            view?.showTokens(homeToken, areZerosHidden)

            // TODO move this to CryptoScreen!
            /*val mappedItems: List<AnyCellItem> = homeMapper.mapToCellItems(
                tokens = state.tokens,
                ethereumTokens = state.ethTokens,
                visibilityState = state.visibilityState,
                isZerosHidden = areZerosHidden,
            )
            view?.showItems(mappedItems)*/
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
}
