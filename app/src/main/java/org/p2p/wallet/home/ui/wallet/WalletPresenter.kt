package org.p2p.wallet.home.ui.wallet

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton.BUY_BUTTON
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton.SELL_BUTTON
import org.p2p.wallet.home.model.HomePresenterMapper
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState
import org.p2p.wallet.utils.ellipsizeAddress
import org.p2p.wallet.utils.unsafeLazy

class WalletPresenter(
    // interactors
    private val homeInteractor: HomeInteractor,
    private val connectionManager: ConnectionManager,
    // mappers
    private val homeMapper: HomePresenterMapper,
    tokenKeyProvider: TokenKeyProvider,
    private val tokenServiceCoordinator: TokenServiceCoordinator
) : BasePresenter<WalletContract.View>(), WalletContract.Presenter {

    private var username: Username? = null

    private val refreshingFlow = MutableStateFlow(true)

    private val userPublicKey: String by unsafeLazy { tokenKeyProvider.publicKey }

    override fun attach(view: WalletContract.View) {
        super.attach(view)
        observeRefreshingStatus()

        loadInitialData()

        observeUsdc()
    }

    private fun observeUsdc() {
        launch {
            tokenServiceCoordinator.observeUserTokens()
                .collect { handleTokenState(it) }
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
                if (usdc != null) {
                    view?.showBalance(homeMapper.mapBalance(usdc.total))
                } else {
                    view?.showBalance(null)
                }
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
        // TODO
    }

    override fun onSendClicked(clickSource: SearchOpenedFromScreen) {
        // TODO
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
