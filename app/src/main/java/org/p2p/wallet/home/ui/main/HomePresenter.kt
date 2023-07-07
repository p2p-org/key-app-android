package org.p2p.wallet.home.ui.main

import androidx.lifecycle.LifecycleOwner
import android.content.Context
import android.content.res.Resources
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.core.utils.Constants.USDT_SYMBOL
import org.p2p.core.utils.Constants.WETH_SYMBOL
import org.p2p.core.utils.asUsd
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.events.AppLoader
import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.HomePresenterMapper
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.main.models.HomeScreenViewState
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.intercom.IntercomDeeplinkManager
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.user.interactor.UserInteractor
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
    private val homeInteractor: HomeInteractor,
    private val userInteractor: UserInteractor,
    private val deeplinksManager: AppDeeplinksManager,
    private val connectionManager: ConnectionManager,
    private val transactionManager: TransactionManager,
    private val intercomDeeplinkManager: IntercomDeeplinkManager,
    private val homeMapper: HomePresenterMapper,
    private val newBuyFeatureToggle: NewBuyFeatureToggle,
    private val analytics: HomeAnalytics,
    private val homeStateLoaders: List<AppLoader>,
    context: Context
) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {

    private val resources: Resources = context.resources
    private var state = HomeScreenViewState(areZerosHidden = homeInteractor.areZerosHidden())

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
        launchSupervisor {
            homeStateLoaders.map { async { it.onLoad() } }.joinAll()
        }
    }

    override fun attach(view: HomeContract.View) {
        super.attach(view)
        handleDeeplinks()
        launch {
            homeInteractor.observeHomeScreenState().filterNotNull()
                .collect { homeState ->
                    val isRefreshing = homeState.isRefreshing
                    view?.showRefreshing(isRefreshing)

                    val username = homeState.username
                    view?.showUserAddress(username)

                    val userBalance = homeState.userBalance
                    view?.showBalance(userBalance?.let { homeMapper.mapBalance(it) })

                    val strigaTokens = homeState.strigaTokens
                    state = state.copy(strigaClaimableTokens = strigaTokens.tokens)

                    val strigaKycBanner = homeState.strigaBanner
                    state = state.copy(strigaKycStatusBanner = strigaKycBanner?.banner)
                    if (!homeState.solanaTokens.isLoading || !homeState.ethTokens.isLoading) {
                        val solanaTokens = homeState.solanaTokens
                        state = state.copy(tokens = solanaTokens.tokens)

                        val ethTokens = homeState.ethTokens
                        state = state.copy(ethTokens = ethTokens.tokens)

                        handleHomeStateChanged(state)
                    }
                    val actionButtons = homeState.actionButtons
                    view?.showActionButtons(actionButtons)
                }
        }
    }

    override fun refreshTokens() {
        launchInternetAware(connectionManager) {
            try {
                homeInteractor.updateRefreshState(isRefreshing = true)
                homeStateLoaders.map { async { it.onRefresh() } }.joinAll()
            } catch (cancelled: CancellationException) {
                Timber.i("Loading tokens job cancelled")
            } catch (error: Throwable) {
                Timber.e(error, "Error refreshing user tokens")
                view?.showErrorMessage(error)
            } finally {
                homeInteractor.updateRefreshState(isRefreshing = false)
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        intercomDeeplinkManager.proceedDeeplinkIfExists()
    }

    override fun load() {
        launch {
            state = state.copy(visibilityState = VisibilityState.create(homeInteractor.getHiddenTokensVisibility()))
            val userId = homeInteractor.getUsernameOrPublicAddress()
            IntercomService.signIn(userId)
        }
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
                        handleHomeStateChanged(state)

                        homeInteractor.loadStrigaFiatAccountDetails()
                            .onSuccess { view?.navigateToKycStatus(statusFromKycBanner) }
                            .onFailure { view?.showUiKitSnackBar(messageResId = R.string.error_general_message) }

                        state = state.copy(isStrigaKycBannerLoading = false)
                        handleHomeStateChanged(state)
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

    override fun onStrigaClaimTokenClicked(item: HomeElementItem.StrigaClaim) {
        launch {
            try {
                view?.showStrigaClaimProgress(isClaimInProgress = true, tokenMint = item.tokenMintAddress)
                val challengeId = homeInteractor.claimStrigaToken(item.amountAvailable, item.strigaToken).unwrap()
                view?.navigateToStrigaClaimOtp(
                    item.amountAvailable.asUsd(),
                    challengeId
                )
            } catch (e: Throwable) {
                Timber.e(e, "Error on claiming striga token")
                if (BuildConfig.DEBUG) {
                    view?.showErrorMessage(IllegalStateException("Striga claiming is not supported yet", e))
                } else {
                    view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                }
            } finally {
                view?.showStrigaClaimProgress(isClaimInProgress = false, tokenMint = item.tokenMintAddress)
            }
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

    override fun onAddressClicked() {
        launch {
            view?.showAddressCopied(homeInteractor.getUsernameOrPublicAddress())
        }
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

    private suspend fun handleHomeStateChanged(newState: HomeScreenViewState) {
        val isAccountEmpty = newState.tokens.all(Token.Active::isZero) && newState.ethTokens.isEmpty()
        when {
            isAccountEmpty -> {
                view?.showEmptyState(isEmpty = true)
                handleEmptyAccount()
            }

            (newState.tokens.isNotEmpty() || newState.ethTokens.isNotEmpty()) -> {
                view?.showEmptyState(isEmpty = false)
                showTokensAndBalance()
            }
        }
    }

    private suspend fun handleEmptyAccount() {
        val tokensForBuy =
            homeInteractor.findMultipleTokenData(POPULAR_TOKENS_SYMBOLS.toList())
                .sortedBy { tokenToBuy -> POPULAR_TOKENS_SYMBOLS.indexOf(tokenToBuy.tokenSymbol) }

        val strigaBigBanner = state.strigaKycStatusBanner
            ?.let { homeMapper.mapToBigBanner(it, state.isStrigaKycBannerLoading) }
            ?: getDefaultBanner()

        val emptyDataList = buildList {
            this += strigaBigBanner
            this += resources.getString(R.string.main_popular_tokens_header)
            addAll(tokensForBuy)
        }
        view?.showEmptyViewData(emptyDataList)

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
            state = state.copy(tokens = updatedTokens)
            handleHomeStateChanged(state)
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
            val strigaBanner = state.strigaKycStatusBanner
                ?.let { homeMapper.mapToHomeBanner(isLoading = state.isStrigaKycBannerLoading, it) }
            val homeToken = buildList {
                if (strigaBanner != null) {
                    this += HomeElementItem.Banner(strigaBanner)
                }
                addAll(mappedTokens)
            }
            view?.showTokens(homeToken, areZerosHidden)
        }
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
}
