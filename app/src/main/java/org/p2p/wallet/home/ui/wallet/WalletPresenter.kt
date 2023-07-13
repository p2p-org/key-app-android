package org.p2p.wallet.home.ui.wallet

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.utils.asUsd
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton.BUY_BUTTON
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton.SELL_BUTTON
import org.p2p.wallet.home.model.HomePresenterMapper
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.kyc.model.StrigaBanner
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState
import org.p2p.wallet.utils.ellipsizeAddress
import org.p2p.wallet.utils.unsafeLazy

class WalletPresenter(
    private val homeInteractor: HomeInteractor,
    private val homeMapper: HomePresenterMapper,
    private val walletMapper: WalletPresenterMapper,
    tokenKeyProvider: TokenKeyProvider,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val strigaOnRampInteractor: StrigaOnRampInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
) : BasePresenter<WalletContract.View>(), WalletContract.Presenter {

    private var username: Username? = null

    private val refreshingFlow = MutableStateFlow(true)
    private val viewStateFlow = MutableStateFlow(WalletViewState())

    private val userPublicKey: String by unsafeLazy { tokenKeyProvider.publicKey }

    override fun firstAttach() {
        super.firstAttach()
        loadStrigaOnRampTokens()
    }

    override fun attach(view: WalletContract.View) {
        super.attach(view)
        observeRefreshingStatus()
        observeScreenState()

        loadInitialData()
        observeRefreshingStatus()
        observeUsdc()
        observeStrigaKycBanners()
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
                .collect { banner ->
                    viewStateFlow.emit(
                        viewStateFlow.value.copy(
                            strigaBanner = banner
                        )
                    )
                }
        }
    }

    private fun observeScreenState() {
        launch {
            viewStateFlow.collect {
                val items = walletMapper.buildCellItems {
                    // order is matter
                    mapStrigaKycBanner(it.strigaBanner)
                    mapStrigaOnRampTokens(it.strigaOnRampTokens)
                }
                view?.setCellItems(items)
            }
        }
    }

    private fun handleTokenState(newState: UserTokensState) {
        view?.showRefreshing(isRefreshing = newState.isLoading())
        when (newState) {
            is UserTokensState.Idle -> Unit
            is UserTokensState.Loading -> Unit
            is UserTokensState.Refreshing -> Unit
            is UserTokensState.Error -> view?.showErrorMessage(newState.cause)
            is UserTokensState.Empty -> view?.showBalance(null)
            is UserTokensState.Loaded -> {
                val usdc = newState.solTokens.find { it.isUSDC }
                val balance = usdc?.total ?: BigDecimal.ZERO
                view?.showBalance(homeMapper.mapBalance(balance))
            }
        }
    }

    private fun observeRefreshingStatus() {
        refreshingFlow
            .onEach {
                view?.showRefreshing(it)
            }
            .launchIn(this)
    }

    private fun loadInitialData() {
        launch {
            val buttons = listOf(BUY_BUTTON, SELL_BUTTON)
            view?.showActionButtons(buttons)

            showUserAddressAndUsername()

            val userId = username?.value ?: userPublicKey
            IntercomService.signIn(userId)
        }
    }

    private fun showUserAddressAndUsername() {
        this.username = homeInteractor.getUsername()
        val userAddress = username?.fullUsername ?: userPublicKey.ellipsizeAddress()
        view?.showUserAddress(userAddress)
    }

    override fun onAddressClicked() {
        view?.showAddressCopied(username?.fullUsername ?: userPublicKey)
    }

    override fun refreshTokens() {
        // TODO
    }

    override fun onBuyClicked() {
        // TODO
    }

    override fun onSellClicked() {
        // TODO
    }

    override fun onSwapClicked() {
        // TODO
    }

    override fun onTopupClicked() {
        view?.showTopupWalletDialog()
    }

    override fun onSendClicked(clickSource: SearchOpenedFromScreen) {
        // TODO
    }

    override fun onStrigaOnRampClicked(item: StrigaOnRampCellModel) {
        launch {
            try {
                view?.showStrigaOnRampProgress(isLoading = true, tokenMint = item.tokenMintAddress)
                val challengeId = strigaOnRampInteractor.onRampToken(item.amountAvailable, item.payload).unwrap()
                view?.navigateToStrigaOnRampOtp(
                    item.amountAvailable.asUsd(),
                    challengeId
                )
            } catch (e: Throwable) {
                Timber.e(e, "Error on on-ramping striga token")
                if (BuildConfig.DEBUG) {
                    view?.showErrorMessage(IllegalStateException("Striga Claim is not supported yet", e))
                } else {
                    view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                }
            } finally {
                view?.showStrigaOnRampProgress(isLoading = false, tokenMint = item.tokenMintAddress)
            }
        }
    }

    override fun onStrigaBannerClicked(item: StrigaBanner) {
        with(item.status) {
            val statusFromKycBanner = homeMapper.getKycStatusBannerFromTitle(bannerTitleResId)
            when {
                statusFromKycBanner == StrigaKycStatusBanner.PENDING -> {
                    view?.showKycPendingDialog()
                }
                statusFromKycBanner != null -> {
                    launch {
                        // hide banner if necessary
                        homeInteractor.hideStrigaUserStatusBanner(statusFromKycBanner)

                        if (statusFromKycBanner == StrigaKycStatusBanner.VERIFICATION_DONE) {
                            view?.showStrigaBannerProgress(isLoading = true)

                            homeInteractor.loadDetailsForStrigaAccounts()
                                .onSuccess { view?.navigateToStrigaByBanner(statusFromKycBanner) }
                                .onFailure { view?.showUiKitSnackBar(messageResId = R.string.error_general_message) }

                            view?.showStrigaBannerProgress(isLoading = false)
                        } else {
                            view?.navigateToStrigaByBanner(statusFromKycBanner)
                        }
                    }
                }
                else -> {
                    view?.showTopupWalletDialog()
                }
            }
        }
    }

    override fun onProfileClick() {
        if (homeInteractor.isUsernameExist()) {
            view?.navigateToProfile()
        } else {
            view?.navigateToReserveUsername()
        }
    }

    private fun showRefreshing(isRefreshing: Boolean) = refreshingFlow.tryEmit(isRefreshing)
}
