package org.p2p.wallet.home.ui.wallet

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.model.HomePresenterMapper
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.utils.ellipsizeAddress
import org.p2p.wallet.utils.unsafeLazy

class WalletPresenter(
    // interactors
    private val homeInteractor: HomeInteractor,
    private val connectionManager: ConnectionManager,
    // mappers
    private val homeMapper: HomePresenterMapper,
    tokenKeyProvider: TokenKeyProvider,
) : BasePresenter<WalletContract.View>(), WalletContract.Presenter {

    private var username: Username? = null

    private val buttonsStateFlow = MutableStateFlow<List<ActionButton>>(emptyList())
    private val refreshingFlow = MutableStateFlow(true)

    private val userPublicKey: String by unsafeLazy { tokenKeyProvider.publicKey }

    override fun attach(view: WalletContract.View) {
        super.attach(view)
        observeRefreshingStatus()
        observeActionButtonState()

        loadInitialData()
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

    private fun loadInitialData() {
        launch {
            initializeActionButtons()
            showUserAddressAndUsername()

            val userId = username?.value ?: userPublicKey
            IntercomService.signIn(userId)

            showTokensAndBalance()
            showRefreshing(false)
        }
    }

    private suspend fun initializeActionButtons(isRefreshing: Boolean = false) {
        if (!isRefreshing && buttonsStateFlow.value.isNotEmpty()) {
            return
        }

        val buttons = mutableListOf(ActionButton.BUY_BUTTON, ActionButton.SELL_BUTTON)
        buttonsStateFlow.emit(buttons)
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

    private fun showTokensAndBalance() {
        launchInternetAware(connectionManager) {
            val balance = getUserBalance()

            if (balance != null) {
                view?.showBalance(homeMapper.mapBalance(balance))
            } else {
                view?.showBalance(null)
            }
        }
    }

    private fun getUserBalance(): BigDecimal? {
        // TODO
        return BigDecimal.ZERO
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
