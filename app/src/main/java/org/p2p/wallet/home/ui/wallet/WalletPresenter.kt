package org.p2p.wallet.home.ui.wallet

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.p2p.core.token.filterTokensForWalletScreen
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.home.ui.main.striga.StrigaOnRampConfirmedHandler
import org.p2p.wallet.home.ui.wallet.analytics.MainScreenAnalytics
import org.p2p.wallet.home.ui.wallet.handlers.StrigaBannerClickHandler
import org.p2p.wallet.home.ui.wallet.handlers.StrigaOnRampClickHandler
import org.p2p.wallet.home.ui.wallet.mapper.WalletMapper
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaBanner
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState
import org.p2p.wallet.utils.ellipsizeAddress
import org.p2p.wallet.utils.unsafeLazy

class WalletPresenter(
    private val usernameInteractor: UsernameInteractor,
    private val walletMapper: WalletMapper,
    tokenKeyProvider: TokenKeyProvider,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val strigaOnRampInteractor: StrigaOnRampInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaBannerClickHandler: StrigaBannerClickHandler,
    private val strigaOnRampClickHandler: StrigaOnRampClickHandler,
    private val strigaOnRampConfirmedHandler: StrigaOnRampConfirmedHandler,
    private val strigaSignupEnabledFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val sellInteractor: SellInteractor,
    private val mainScreenAnalytics: MainScreenAnalytics,
) : BasePresenter<WalletContract.View>(), WalletContract.Presenter {

    private var username: Username? = null

    private val viewStateFlow = MutableStateFlow(WalletViewState())

    private val userPublicKey: String by unsafeLazy { tokenKeyProvider.publicKey }

    override fun firstAttach() {
        super.firstAttach()
        loadStrigaOnRampTokens()
    }

    override fun attach(view: WalletContract.View) {
        super.attach(view)
        observeViewState()

        loadInitialData()
        observeUsdc()
        observeStrigaKycBanners()

        view.setWithdrawButtonIsVisible(strigaSignupEnabledFeatureToggle.isFeatureEnabled)
        launch {
            mainScreenAnalytics.logMainScreenOpen(isSellEnabled = sellInteractor.isSellAvailable())
        }
    }

    private fun observeUsdc() {
        launch {
            tokenServiceCoordinator.observeUserTokens()
                .collect { handleTokenState(it) }
        }
    }

    private fun loadStrigaOnRampTokens() {
        launch {
            val strigaOnRampTokens = strigaOnRampInteractor.getOnRampTokens().successOrNull().orEmpty()
            viewStateFlow.emit(
                viewStateFlow.value.copy(strigaOnRampTokens = strigaOnRampTokens)
            )
        }
    }

    private fun observeStrigaKycBanners() {
        launch {
            strigaUserInteractor.getUserStatusBannerFlow()
                .map { viewStateFlow.value.copy(strigaBanner = it) }
                .collect { viewStateFlow.emit(it) }
        }
    }

    private fun observeViewState() {
        launch {
            viewStateFlow.collect {
                val items = walletMapper.buildCellItems {
                    // order matters
                    val strigaClaimTokens = it.strigaOnRampTokens
                    mapStrigaKycBanner(it.strigaBanner)
                    mapStrigaOnRampTokens(strigaClaimTokens)
                    if (strigaClaimTokens.isNotEmpty()) {
                        mainScreenAnalytics.logMainScreenClaimTransferedViewed(claimCount = strigaClaimTokens.size)
                    }
                }
                view?.setCellItems(items)
                view?.showEmptyState(items.isEmpty())
            }
        }
    }

    private fun handleTokenState(newState: UserTokensState) {
        view?.showRefreshing(isRefreshing = newState.isLoading())
        when (newState) {
            is UserTokensState.Idle -> Unit
            is UserTokensState.Loading -> {
                view?.showBalance(
                    walletMapper.getFiatBalanceSkeleton(),
                    walletMapper.getTokenBalanceSkeleton()
                )
            }
            is UserTokensState.Refreshing -> Unit
            is UserTokensState.Error -> view?.showErrorMessage(newState.cause)
            is UserTokensState.Empty -> {
                view?.showBalance(
                    walletMapper.mapFiatBalance(BigDecimal.ZERO),
                    walletMapper.mapTokenBalance(BigDecimal.ZERO)
                )
            }
            is UserTokensState.Loaded -> {
                val filteredTokens = newState.solTokens.filterTokensForWalletScreen()
                val balance = filteredTokens.sumOf { it.total }
                mainScreenAnalytics.logUserAggregateBalanceBase(balance)
                view?.showBalance(
                    walletMapper.mapFiatBalance(balance),
                    walletMapper.mapTokenBalance(balance)
                )
            }
        }
    }

    private fun loadInitialData() {
        launch {
            showUserAddressAndUsername()

            val userId = username?.value ?: userPublicKey
            IntercomService.signIn(userId)
        }
    }

    private fun showUserAddressAndUsername() {
        this.username = usernameInteractor.getUsername()
        val userAddress = username?.fullUsername ?: userPublicKey.ellipsizeAddress()
        view?.showUserAddress(userAddress)
    }

    override fun onAddressClicked() {
        mainScreenAnalytics.logMainScreenAddressClick()
        val fullUsername = username?.fullUsername
        val hasUserName = !fullUsername.isNullOrEmpty()
        view?.showAddressCopied(
            addressOrUsername = fullUsername ?: userPublicKey,
            stringResId = if (hasUserName) {
                R.string.wallet_username_copy_snackbar_text
            } else {
                R.string.wallet_address_copy_snackbar_text
            }
        )
    }

    override fun onAmountClicked() {
        mainScreenAnalytics.logMainScreenAmountClick()
    }

    override fun refreshTokens() {
        tokenServiceCoordinator.refresh()
    }

    override fun onWithdrawClicked() {
        mainScreenAnalytics.logMainScreenWithdrawClick()
        view?.navigateToOffRamp()
    }

    override fun onAddMoneyClicked() {
        mainScreenAnalytics.logMainScreenAddMoneyClick()
        view?.showTopupWalletDialog()
    }

    override fun onStrigaOnRampClicked(item: StrigaOnRampCellModel) {
        mainScreenAnalytics.logMainScreenClaimTransferedClick()
        launch {
            strigaOnRampClickHandler.handle(view, item)
        }
    }

    override fun onStrigaBannerClicked(item: StrigaBanner) {
        launch {
            strigaBannerClickHandler.handle(view, item)
        }
    }

    override fun onOnRampConfirmed(
        challengeId: StrigaWithdrawalChallengeId,
        token: StrigaOnRampCellModel
    ) {
        launch {
            strigaOnRampConfirmedHandler.handleConfirmed(token)
        }
    }

    override fun onProfileClick() {
        if (usernameInteractor.isUsernameExist()) {
            view?.navigateToProfile()
        } else {
            view?.navigateToReserveUsername()
        }
    }
}
