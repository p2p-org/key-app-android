package org.p2p.wallet.home.ui.crypto

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.model.HomePresenterMapper
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState
import org.p2p.wallet.transaction.model.TransactionState

class MyCryptoPresenter(
    private val homeInteractor: HomeInteractor,
    private val homeMapper: HomePresenterMapper,
    private val connectionManager: ConnectionManager,
    private val transactionManager: TransactionManager,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val analytics: HomeAnalytics,
) : BasePresenter<MyCryptoContract.View>(), MyCryptoContract.Presenter {

    private var visibilityState: VisibilityState = VisibilityState.Hidden
    private var cryptoTokensSubscription: Job? = null

    override fun attach(view: MyCryptoContract.View) {
        super.attach(view)
        prepareAndShowActionButtons()
        observeCryptoTokens()
    }

    override fun refreshTokens() {
        launchInternetAware(connectionManager) {
            try {
                tokenServiceCoordinator.refresh()
            } catch (cancelled: CancellationException) {
                Timber.i("Loading tokens job cancelled")
            } catch (error: Throwable) {
                Timber.e(error, "Error refreshing user tokens")
                view?.showErrorMessage(error)
            }
        }
    }

    private fun observeCryptoTokens() {
        cryptoTokensSubscription?.cancel()
        cryptoTokensSubscription = launch {
            tokenServiceCoordinator.observeUserTokens()
                .collect { handleTokenState(it) }
        }
    }

    private fun handleTokenState(newState: UserTokensState) {
        Timber.tag("_____").d(newState.toString())
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
                showTokensAndBalance(
                    solTokens = newState.solTokens,
                    ethTokens = newState.ethTokens
                )
            }
        }
    }

    private fun showTokensAndBalance(solTokens: List<Token.Active>, ethTokens: List<Token.Eth>) {
        val balance = getUserBalance(solTokens)
        view?.showBalance(homeMapper.mapBalance(balance))
        logBalance(balance)

        val areZerosHidden = homeInteractor.areZerosHidden()
        val mappedItems: List<AnyCellItem> = homeMapper.mapToCellItems(
            tokens = solTokens,
            ethereumTokens = ethTokens,
            visibilityState = visibilityState,
            isZerosHidden = areZerosHidden,
        )
        view?.showItems(mappedItems)
    }

    private fun getUserBalance(tokens: List<Token.Active>): BigDecimal {
        if (tokens.none { it.totalInUsd != null }) return BigDecimal.ZERO

        return tokens
            .mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()
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
        }
    }

    override fun toggleTokenVisibilityState() {
        visibilityState = visibilityState.toggle()
        homeInteractor.setHiddenTokensVisibility(visibilityState.isVisible)
        observeCryptoTokens()
    }

    private fun logBalance(balance: BigDecimal?) {
        val hasPositiveBalance = balance != null && balance.isMoreThan(BigDecimal.ZERO)
        analytics.logUserHasPositiveBalanceProperty(hasPositiveBalance)
        analytics.logUserAggregateBalanceProperty(balance.orZero())
    }

    private fun prepareAndShowActionButtons() {
        val buttons = listOf(ActionButton.RECEIVE_BUTTON, ActionButton.SWAP_BUTTON)
        view?.showActionButtons(buttons)
    }

    override fun onReceiveClicked() {
        view?.navigateToReceive()
    }

    override fun onSwapClicked() {
        analytics.logSwapActionButtonClicked()
        view?.navigateToSwap()
    }

    override fun onClaimClicked(canBeClaimed: Boolean, token: Token.Eth) {
        launch {
            analytics.logClaimButtonClicked()
            if (canBeClaimed) {
                view?.navigateToTokenClaim(token)
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
}
