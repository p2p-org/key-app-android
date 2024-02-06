package org.p2p.wallet.history.ui.token

import timber.log.Timber
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.core.utils.asNegativeUsdTransaction
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.ui.mapper.ClaimUiMapper
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.bridge.send.ui.mapper.BridgeSendUiMapper
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.pnl.interactor.PnlDataObserver
import org.p2p.wallet.pnl.ui.PnlUiMapper
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.transaction.model.progressstate.TransactionState
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserTokensLocalRepository

class TokenHistoryPresenter(
    // has old data inside, use userTokensRepository to get fresh one
    private var token: Token.Active,
    private val tokenInteractor: TokenInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val transactionManager: TransactionManager,
    private val claimUiMapper: ClaimUiMapper,
    private val userRepository: UserLocalRepository,
    private val bridgeSendUiMapper: BridgeSendUiMapper,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle,
    private val userTokensRepository: UserTokensLocalRepository,
    private val historyAnalytics: HistoryAnalytics,
    private val pnlInteractor: PnlDataObserver,
    private val pnlUiMapper: PnlUiMapper,
) : BasePresenter<TokenHistoryContract.View>(), TokenHistoryContract.Presenter {

    override fun attach(view: TokenHistoryContract.View) {
        super.attach(view)
        observeCryptoTokens()
        observePnlData()
        initialize()
    }

    private fun observeCryptoTokens() {
        userTokensRepository.observeUserToken(token.mintAddress.toBase58Instance())
            .onEach { updateTokenAmounts(it) }
            .launchIn(this)
    }

    private fun observePnlData() {
        pnlInteractor.pnlState
            .onEach { updateTokenAmounts(this.token) }
            .launchIn(this)
    }

    private fun initialize() {
        updateTokenAmounts(token)

        val actionButtons = buildList {
            this += ActionButton.RECEIVE_BUTTON
            this += ActionButton.SWAP_BUTTON
            this += ActionButton.SEND_BUTTON

            // moonpay accepts only SOL for selling
            if (token.isSOL) {
                this += ActionButton.SELL_BUTTON
            }
            // SOL or USDC can be bought
            if (token.isSOL || token.isUSDC) {
                this += ActionButton.BUY_BUTTON
            }
        }.sorted()

        view?.showActionButtons(actionButtons)
    }

    private fun updateTokenAmounts(token: Token.Active) {
        this.token = token
        view?.renderTokenAmounts(token)
        view?.renderTokenPnl(
            pnlUiMapper.mapTokenBalancePnl(
                tokenMint = token.mintAddressB58,
                pnlDataState = pnlInteractor.pnlState.value
            )
        )
    }

    override fun onTokenPnlClicked() {
        if (pnlInteractor.pnlState.value.isLoaded()) {
            pnlInteractor.pnlState.value.findForToken(token.mintAddressB58)?.let {
                view?.showPnlDetails(it.percent)
            }
        }
    }

    override fun onRefresh() {
        pnlInteractor.restartAndRefresh()
    }

    override fun onTransactionClicked(transactionId: String) {
        historyAnalytics.logTokenTransactionClicked(transactionId)
        view?.showDetailsScreen(transactionId)
    }

    override fun onSellTransactionClicked(transactionId: String) {
        view?.openSellTransactionDetails(transactionId)
    }

    override fun onBridgePendingClaimClicked(transactionId: String) {
        launch {
            val bridgeBundle = ethereumInteractor.getClaimBundleById(transactionId) ?: return@launch
            val claimDetails = claimUiMapper.makeClaimDetails(
                bridgeBundle = bridgeBundle,
                minAmountForFreeFee = ethereumInteractor.getClaimMinAmountForFreeFee(),
            )
            val amountToClaim = bridgeBundle.resultAmount.amountInToken
            val iconUrl = ERC20Tokens.findToken(bridgeBundle.findTokenOrDefaultEth().hex).tokenIconUrl

            val progressDetails = claimUiMapper.prepareShowProgress(
                amountToClaim = amountToClaim,
                iconUrl = iconUrl,
                claimDetails = claimDetails
            )
            transactionManager.emitTransactionState(
                transactionId,
                TransactionState.Progress(description = R.string.bridge_claim_description_progress)
            )
            view?.showProgressDialog(
                bundleId = bridgeBundle.bundleId,
                progressDetails = progressDetails
            )
        }
    }

    override fun onBridgePendingSendClicked(transactionId: String) {
        launch {
            val sendBundle = ethereumInteractor.getSendBundleById(transactionId) ?: return@launch
            val token = userRepository.getTokensData().firstOrNull { it.mintAddress == sendBundle.recipient.raw }
            val feeDetails = bridgeSendUiMapper.makeBridgeFeeDetails(
                recipientAddress = sendBundle.recipient.raw,
                fees = sendBundle.fees
            )
            val progressDetails = bridgeSendUiMapper.prepareShowProgress(
                iconUrl = token?.iconUrl.orEmpty(),
                amountTokens = "${sendBundle.amount.amountInToken.toPlainString()} ${token?.symbol}",
                amountUsd = sendBundle.amount.amountInUsd.toBigDecimalOrZero().asNegativeUsdTransaction(),
                recipient = sendBundle.recipient.raw,
                feeDetails = feeDetails
            )
            val progressState = TransactionState.Progress(
                description = R.string.bridge_send_transaction_description_progress
            )

            transactionManager.emitTransactionState(transactionId, progressState)
            view?.showProgressDialog(transactionId, progressDetails)
        }
    }

    override fun closeAccount() {
        launch {
            try {
                tokenInteractor.closeTokenAccount(token.publicKey)
                view?.showUiKitSnackBar(messageResId = R.string.details_account_closed_successfully)
            } catch (e: Throwable) {
                Timber.e(e, "Error closing account: ${token.publicKey}")
                view?.showErrorMessage(e)
            }
        }
    }

    override fun onReceiveClicked() {
        if (ethAddressEnabledFeatureToggle.isFeatureEnabled) {
            val erc20TokensMints = ERC20Tokens.values().flatMap(ERC20Tokens::receiveFromTokens)
            if (token.mintAddress in erc20TokensMints) {
                view?.showReceiveNetworkDialog()
            } else {
                view?.openReceiveInSolana()
            }
        } else {
            view?.openOldReceiveInSolana()
        }
    }
}
