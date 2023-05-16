package org.p2p.wallet.history.ui.token

import timber.log.Timber
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
import org.p2p.wallet.home.repository.UserTokensRepository
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.utils.toBase58Instance
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.user.repository.UserLocalRepository

class TokenHistoryPresenter(
    // has old data inside, use userTokensRepository to get fresh one
    private val token: Token.Active,
    private val tokenInteractor: TokenInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val transactionManager: TransactionManager,
    private val claimUiMapper: ClaimUiMapper,
    private val userRepository: UserLocalRepository,
    private val bridgeSendUiMapper: BridgeSendUiMapper,
    private val ethAddressEnabledFeatureToggle: EthAddressEnabledFeatureToggle,
    private val userTokensRepository: UserTokensRepository
) : BasePresenter<TokenHistoryContract.View>(), TokenHistoryContract.Presenter {

    override fun attach(view: TokenHistoryContract.View) {
        super.attach(view)
        subscribeToTokenUpdates()
        initialize()
    }

    private fun subscribeToTokenUpdates() {
        userTokensRepository.observeUserToken(token.mintAddress.toBase58Instance())
            .onEach { view?.renderTokenAmounts(it) }
            .launchIn(this)
    }

    private fun initialize() {
        view?.renderTokenAmounts(token)

        val actionButtons = mutableListOf(
            ActionButton.RECEIVE_BUTTON,
            ActionButton.SEND_BUTTON,
            ActionButton.SWAP_BUTTON
        )

        if (token.isSOL || token.isUSDC) {
            actionButtons.add(0, ActionButton.BUY_BUTTON)
        }

        view?.showActionButtons(actionButtons)
    }

    override fun onTransactionClicked(transactionId: String) {
        view?.showDetailsScreen(transactionId)
    }

    override fun onSellTransactionClicked(transactionId: String) {
        view?.openSellTransactionDetails(transactionId)
    }

    override fun onBridgePendingClaimClicked(transactionId: String) {
        launch {
            val bridgeBundle = ethereumInteractor.getClaimBundleById(transactionId) ?: return@launch
            val claimDetails = claimUiMapper.makeClaimDetails(
                resultAmount = bridgeBundle.resultAmount,
                fees = bridgeBundle.fees,
                isFree = bridgeBundle.compensationDeclineReason.isEmpty(),
                minAmountForFreeFee = ethereumInteractor.getClaimMinAmountForFreeFee(),
                transactionDate = bridgeBundle.dateCreated
            )
            val amountToClaim = bridgeBundle.resultAmount.amountInToken
            val iconUrl = ERC20Tokens.findToken(bridgeBundle.findTokenOrDefaultEth()).tokenIconUrl

            val progressDetails = claimUiMapper.prepareShowProgress(
                amountToClaim = amountToClaim,
                iconUrl = iconUrl.orEmpty(),
                claimDetails = claimDetails
            )
            transactionManager.emitTransactionState(
                transactionId,
                TransactionState.ClaimProgress(transactionId)
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
