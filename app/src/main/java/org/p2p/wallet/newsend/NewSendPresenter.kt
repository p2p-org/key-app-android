package org.p2p.wallet.newsend

import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.emptyString
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransferType
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.NewSendButton
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.CurrencyMode
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.toPublicKey
import org.threeten.bp.ZonedDateTime
import java.math.BigInteger
import java.util.UUID
import kotlin.properties.Delegates
import kotlinx.coroutines.launch

class NewSendPresenter(
    private val recipientAddress: SearchResult,
    private val userInteractor: UserInteractor,
    private val sendInteractor: SendInteractor,
    private val resources: ResourcesProvider,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionManager: TransactionManager
) : BasePresenter<NewSendContract.View>(), NewSendContract.Presenter {

    private var token: Token.Active? by Delegates.observable(null) { _, _, newToken ->
        if (newToken != null) {
            view?.showToken(newToken)
            calculationMode.updateToken(newToken)
        }
    }

    private var inputAmount: String by Delegates.observable(emptyString()) { _, _, newInput ->
        calculationMode.updateInputAmount(newInput)
    }

    private val calculationMode = CalculationMode()
    private val feeRelayerState = SendFeeRelayerState(sendInteractor)

    override fun attach(view: NewSendContract.View) {
        super.attach(view)
        initialize(view)
    }

    private fun initialize(view: NewSendContract.View) {
        calculationMode.onOutputCalculated = { view.showAroundValue(it) }
        calculationMode.onLabelsUpdated = { switchSymbol, mainSymbol ->
            view.setSwitchLabel(switchSymbol)
            view.setMainAmountLabel(mainSymbol)
        }

        feeRelayerState.onFeeUpdated = { fee ->
            val text = fee.getTotalFee { resources.getString(it) }
            view.setFeeLabel(text)

            token?.let { updateButton(it, fee.fee) }
        }

        launch {
            // We should find SOL anyway because SOL is needed for Selection Mechanism
            val userTokens = userInteractor.getUserTokens()
            if (userTokens.isEmpty()) {
                // we cannot proceed if user tokens are not loaded
                view.showUiKitSnackBar(resources.getString(R.string.error_general_message))
                return@launch
            }

            val initialToken = userTokens.first()
            token = initialToken
            calculationMode.updateCurrency(CurrencyMode.Token(initialToken.tokenSymbol))

            initializeFeeRelayer(view, initialToken)
        }
    }

    private suspend fun initializeFeeRelayer(
        view: NewSendContract.View,
        initialToken: Token.Active
    ) {
        view.showFeeViewLoading(isLoading = true)
        view.setFeeLabel(resources.getString(R.string.send_fees))
        view.setBottomButtonText(TextContainer.Res(R.string.send_calculating_fees))

        feeRelayerState.initialize(initialToken, recipientAddress)

        view.showFeeViewLoading(isLoading = false)
        updateButton(initialToken, feeRelayerState.getSolanaFee())
    }

    override fun onTokenClicked() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val result = tokens.filter { token -> !token.isZero }
            view?.navigateToTokenSelection(result, token)
        }
    }

    override fun updateToken(newToken: Token.Active) {
        token = newToken
    }

    override fun switchCurrencyMode() {
        calculationMode.switchMode()
    }

    override fun setAmount(amount: String) {
        inputAmount = amount
        showMaxButtonIfNeeded()
        updateButton(requireToken(), feeRelayerState.getSolanaFee())
    }

    override fun setMaxAmountValue() {
        val totalAvailable = calculationMode.getTotalAvailable() ?: return
        view?.updateInputValue(totalAvailable, forced = true)
        inputAmount = totalAvailable.toString()

        showMaxButtonIfNeeded()

        val token = token ?: return
        val message = resources.getString(R.string.send_using_max_amount, token.tokenSymbol)
        view?.showUiKitSnackBar(message)
    }

    override fun onFeeInfoClicked() {
        val fee = feeRelayerState.getFeeTotal()
        if (fee == null) {
            view?.showFreeTransactionsInfo()
        } else {
            view?.showTransactionDetails(fee)
        }
    }

    override fun send() {
        val token = token ?: error("Token cannot be null!")
        val address = recipientAddress.addressState.address
        val currentAmount = calculationMode.getCurrentAmount()
        val lamports = calculationMode.getCurrentAmountLamports()

        // the internal id for controlling the transaction state
        val internalTransactionId = UUID.randomUUID().toString()

        launch {
            try {
                val destinationAddressShort = address.cutMiddle()
                val data = ShowProgress(
                    title = R.string.send_transaction_being_processed,
                    subTitle = "${currentAmount.toPlainString()} ${token.tokenSymbol} → $destinationAddressShort",
                    transactionId = emptyString()
                )

                view?.showProgressDialog(internalTransactionId, data)

                val result = sendInteractor.sendTransaction(address.toPublicKey(), token, lamports)
                val transactionState = TransactionState.SendSuccess(buildTransaction(result), token.tokenSymbol)
                transactionManager.emitTransactionState(internalTransactionId, transactionState)
            } catch (e: Throwable) {
                val message = e.getErrorMessage { res -> resources.getString(res) }
                transactionManager.emitTransactionState(internalTransactionId, TransactionState.Error(message))
            }
        }
    }

    private fun showMaxButtonIfNeeded() {
        view?.setMaxButtonVisible(isVisible = inputAmount.isEmpty())
    }

    private fun buildTransaction(transactionId: String): HistoryTransaction =
        HistoryTransaction.Transfer(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = null,
            type = TransferType.SEND,
            senderAddress = tokenKeyProvider.publicKey,
            tokenData = TokenConverter.toTokenData(token!!),
            totalInUsd = calculationMode.getCurrentAmountUsd(),
            total = calculationMode.getCurrentAmount(),
            destination = recipientAddress.addressState.address,
            fee = BigInteger.ZERO,
            status = TransactionStatus.PENDING
        )

    private fun updateButton(sourceToken: Token.Active, sendFee: SendSolanaFee?) {
        val sendButton = NewSendButton(
            sourceToken = sourceToken,
            searchResult = recipientAddress,
            tokenAmount = calculationMode.getCurrentAmount(),
            sendFee = sendFee,
            minRentExemption = feeRelayerState.getMinRentExemption()
        )

        when (val state = sendButton.state) {
            is NewSendButton.State.Disabled -> {
                view?.setBottomButtonText(TextContainer.Res(state.textResId))
                view?.setSliderText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }
            is NewSendButton.State.Enabled -> {
                view?.setSliderText(resources.getString(state.textResId))
                view?.setBottomButtonText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }
        }
    }

    private fun requireToken(): Token.Active =
        token ?: error("Source token cannot be empty!")
}
