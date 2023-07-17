package org.p2p.wallet.home.ui.wallet

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton.BUY_BUTTON
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton.SELL_BUTTON
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.home.ui.main.striga.StrigaOnRampConfirmedHandler
import org.p2p.wallet.home.ui.wallet.handlers.StrigaBannerClickHandler
import org.p2p.wallet.home.ui.wallet.handlers.StrigaOnRampClickHandler
import org.p2p.wallet.home.ui.wallet.mapper.WalletMapper
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.kyc.model.StrigaBanner
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
            is UserTokensState.Empty -> view?.showBalance(walletMapper.mapBalance(BigDecimal.ZERO))
            is UserTokensState.Loaded -> {
                val usdc = newState.solTokens.find { it.isUSDC }
                val balance = usdc?.total ?: BigDecimal.ZERO
                view?.showBalance(walletMapper.mapBalance(balance))
            }
        }
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
        this.username = usernameInteractor.getUsername()
        val userAddress = username?.fullUsername ?: userPublicKey.ellipsizeAddress()
        view?.showUserAddress(userAddress)
    }

    override fun onAddressClicked() {
        view?.showAddressCopied(username?.fullUsername ?: userPublicKey)
    }

    override fun refreshTokens() {
        tokenServiceCoordinator.refresh()
    }

    override fun onSellClicked() {
        // TODO
    }

    override fun onTopupClicked() {
        view?.showTopupWalletDialog()
    }

    override fun onStrigaOnRampClicked(item: StrigaOnRampCellModel) {
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
