package org.p2p.wallet.bridge.send.ui

import android.content.res.Resources
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.common.di.AppScope
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.asNegativeUsdTransaction
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.formatTokenWithSymbol
import org.p2p.core.utils.isConnectionError
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.bridge.analytics.SendBridgesAnalytics
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.model.toBridgeAmount
import org.p2p.wallet.bridge.send.interactor.BridgeSendInteractor
import org.p2p.wallet.bridge.send.model.getFeeList
import org.p2p.wallet.bridge.send.statemachine.BridgeSendStateMachine
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendFeatureException
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.bridgeFee
import org.p2p.wallet.bridge.send.statemachine.bridgeToken
import org.p2p.wallet.bridge.send.statemachine.inputAmount
import org.p2p.wallet.bridge.send.statemachine.lastStaticState
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendInitialData
import org.p2p.wallet.bridge.send.statemachine.model.SendToken
import org.p2p.wallet.bridge.send.ui.mapper.BridgeSendUiMapper
import org.p2p.wallet.bridge.send.ui.model.BridgeFeeDetails
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.send.model.CalculationMode
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.transaction.model.progressstate.BridgeSendProgressState
import org.p2p.wallet.transaction.model.progressstate.TransactionState
import org.p2p.wallet.updates.NetworkConnectionStateProvider
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.getErrorMessage

private val MAX_FEE_AMOUNT = BigDecimal(30)

class BridgeSendPresenter(
    private val recipientAddress: SearchResult,
    private val userInteractor: UserInteractor,
    private val bridgeInteractor: BridgeSendInteractor,
    private val resources: Resources,
    private val transactionManager: TransactionManager,
    private val connectionStateProvider: NetworkConnectionStateProvider,
    private val sendBridgesAnalytics: SendBridgesAnalytics,
    private val appScope: AppScope,
    sendModeProvider: SendModeProvider,
    private val initialData: SendInitialData.Bridge,
    private val stateMachine: BridgeSendStateMachine,
    private val bridgeSendUiMapper: BridgeSendUiMapper,
    private val alarmErrorsLogger: AlarmErrorsLogger
) : BasePresenter<BridgeSendContract.View>(), BridgeSendContract.Presenter {

    private var currentState: SendState = SendState.Static.Empty

    private val calculationMode = CalculationMode(
        sendModeProvider = sendModeProvider,
        lessThenMinString = resources.getString(R.string.common_less_than_minimum)
    )

    override fun attach(view: BridgeSendContract.View) {
        super.attach(view)
        sendBridgesAnalytics.logSendBridgesScreenOpened()
        initialize(view)
        stateMachine.observe()
            .onEach {
                handleState(it)
                this.currentState = it
            }
            .launchIn(this)
    }

    private fun handleState(state: SendState) {
        when (state) {
            is SendState.Exception -> handleErrorState(state)
            is SendState.Loading -> handleLoadingState(state)
            is SendState.Static -> handleStaticState(state)
        }
    }

    private fun handleStaticState(state: SendState.Static) {
        when (state) {
            SendState.Static.Empty -> Unit
            is SendState.Static.ReadyToSend -> view?.apply {
                val bridgeToken = state.bridgeToken ?: return
                updateTokenAndInput(bridgeToken, state.amount)
                handleUpdateArbiterFee(token = bridgeToken, sendFee = state.fee, isInputEmpty = false)
                handleUpdateTotal(sendFee = state.fee)

                updateButtons(
                    errorButton = null,
                    sliderButton = resources.getString(R.string.bridge_send)
                )
            }

            is SendState.Static.TokenNotZero -> view?.apply {
                val bridgeToken = state.bridgeToken ?: return
                updateTokenAndInput(bridgeToken, state.amount)
                handleUpdateArbiterFee(token = bridgeToken, sendFee = state.fee, isInputEmpty = false)
                handleUpdateTotal(sendFee = state.fee)
                updateButtons(
                    errorButton = TextContainer.Res(R.string.main_enter_the_amount),
                    sliderButton = null
                )
            }

            is SendState.Static.TokenZero -> view?.apply {
                val bridgeToken = state.bridgeToken ?: return
                updateTokenAndInput(bridgeToken, state.inputAmount.orZero())
                handleUpdateArbiterFee(token = bridgeToken, sendFee = state.fee, isInputEmpty = true)
                handleUpdateTotal(sendFee = state.fee)
                updateButtons(
                    errorButton = TextContainer.Res(R.string.main_enter_the_amount),
                    sliderButton = null
                )
            }
        }
        view?.apply {
            showFeeViewVisible(isVisible = true)
            showFeeViewLoading(isLoading = false)
            setInputColor(R.color.text_night)
        }
    }

    private fun handleLoadingState(state: SendState.Loading) {
        handleStaticState(state.lastStaticState)
        view?.apply {
            when (state) {
                is SendState.Loading.Fee -> {
                    showFeeViewVisible(isVisible = true)
                    showFeeViewLoading(isLoading = true)
                    showBottomFeeValue(bridgeSendUiMapper.getFeeTextSkeleton())
                    updateButtons(
                        errorButton = TextContainer.Res(R.string.send_calculating_fees),
                        sliderButton = null
                    )
                }
            }
        }
    }

    private fun handleErrorState(state: SendState.Exception) {
        handleStaticState(state.lastStaticState)
        view?.apply {
            when (state) {
                is SendState.Exception.Feature -> {
                    showFeeViewLoading(isLoading = false)
                    val bridgeToken = state.lastStaticState.bridgeToken ?: return
                    updateTokenAndInput(bridgeToken, state.featureException.amount)
                    handleUpdateTotal(state.lastStaticState.bridgeFee)
                    when (state.featureException) {
                        is SendFeatureException.FeeLoadingError -> {
                            showFeeViewVisible(isVisible = false)
                            showBottomFeeValue(TextViewCellModel.Raw(TextContainer(emptyString())))
                            updateButtons(
                                errorButton = TextContainer.Res(R.string.send_cant_calculate_fees_error),
                                sliderButton = null
                            )
                        }

                        is SendFeatureException.NotEnoughAmount,
                        is SendFeatureException.InsufficientFunds,
                        is SendFeatureException.FeeIsMoreThanAmount -> {
                            setInputColor(R.color.text_rose)
                            updateButtons(
                                errorButton = TextContainer.Res(R.string.send_error_insufficient_funds),
                                sliderButton = null
                            )
                        }
                    }
                }

                is SendState.Exception.Other -> if (state.exception.isConnectionError()) {
                    view?.showUiKitSnackBar(
                        message = resources.getString(R.string.error_no_internet_message),
                        actionButtonResId = R.string.common_hide
                    )
                } else {
                    view?.showUiKitSnackBar(message = state.exception.message)
                }
            }
        }
    }

    // di inject initialData
    override fun setInitialData(selectedToken: Token.Active?, inputAmount: BigDecimal?) = Unit

    private fun initialize(view: BridgeSendContract.View) {
        view.disableSwitchAmounts()

        calculationMode.onCalculationCompleted = { view.showAroundValue(it) }
        calculationMode.onInputFractionUpdated = { view.updateInputFraction(it) }
        calculationMode.onLabelsUpdated = { switchSymbol, mainSymbol ->
            view.setSwitchLabel(switchSymbol)
            view.setMainAmountLabel(mainSymbol)
        }
        val token = initialData.initialToken?.token
        token?.let { calculationMode.updateToken(token) }

        initialData.initialAmount?.let { inputAmount ->
            setupDefaultFields(inputAmount)
        }
        launch {
            val supportedTokens = bridgeInteractor.supportedSendTokens()
            val isTokenChangeEnabled = supportedTokens.size > 1
            view.setTokenContainerEnabled(isEnabled = isTokenChangeEnabled)
            val initialToken = initialData.initialToken?.token ?: supportedTokens.first()
            if (initialToken.rate == null) {
                calculationMode.updateToken(initialToken)
                if (calculationMode.getCurrencyMode() is CurrencyMode.Fiat.Usd) {
                    switchCurrencyMode()
                }
                view.disableSwitchAmounts()
            }
        }
    }

    private fun setupDefaultFields(inputAmount: BigDecimal) {
        view?.apply {
            if (calculationMode.getCurrencyMode() is CurrencyMode.Fiat.Usd) {
                switchCurrencyMode()
            }
            val newTextValue = inputAmount.scaleShort().toPlainString()
            updateInputValue(newTextValue, forced = true)
            calculationMode.updateInputAmount(newTextValue)
            disableInputs()
        }
    }

    private fun BridgeSendContract.View.updateButtons(errorButton: TextContainer?, sliderButton: String?) {
        setSliderText(sliderButton)
        setBottomButtonText(errorButton)
    }

    private fun BridgeSendContract.View.updateTokenAndInput(token: SendToken.Bridge, amount: BigDecimal) {
        showToken(token.token)
        calculationMode.updateToken(token.token)
        val oldInputAmount = calculationMode.formatInputAmount
        calculationMode.updateTokenAmount(amount)
        val inputAmount = calculationMode.formatInputAmount
        if (oldInputAmount != inputAmount && calculationMode.getCurrencyMode() !is CurrencyMode.Fiat) {
            updateInputValue(inputAmount, true)
        }
        val feeTotalInToken = getFeeTotalInToken()
        val validAmount = token.token.total - feeTotalInToken > BigDecimal.ZERO
        view?.setMaxButtonVisible(calculationMode.inputAmountDecimal.isZero() && validAmount)
    }

    private fun BridgeSendContract.View.handleUpdateTotal(sendFee: SendFee?) {
        val amount = calculationMode.inputAmountDecimal.orZero()
        val total = countTotal(amount, bridgeFee = sendFee as? SendFee.Bridge)
        val totalFormatted = if (amount.isZero()) {
            emptyString()
        } else {
            total
        }
        setTotalValue(totalFormatted)
    }

    private fun countTotal(amount: BigDecimal, bridgeFee: SendFee.Bridge?): String {
        val mode = calculationMode.getCurrencyMode()
        val bridgeFeeAmount = bridgeFee?.fee?.totalAmount?.toBridgeAmount()
        return if (bridgeFeeAmount != null && !bridgeFeeAmount.isZero) {
            if (mode is CurrencyMode.Fiat.Usd) {
                bridgeFeeAmount.fiatAmount.orZero().asUsd()
            } else {
                bridgeFeeAmount.formattedTokenAmount.orEmpty()
            }
        } else {
            val totalFeeAmount = if (mode is CurrencyMode.Fiat.Usd) {
                bridgeFeeAmount?.fiatAmount.orZero()
            } else {
                bridgeFeeAmount?.tokenAmount.orZero()
            }
            val amountPlusTotalFee = amount + totalFeeAmount
            when (mode) {
                is CurrencyMode.Fiat.Usd -> {
                    amountPlusTotalFee.asUsd()
                }
                is CurrencyMode.Token -> {
                    val tokenSymbol = mode.symbol
                    amountPlusTotalFee.formatTokenWithSymbol(tokenSymbol)
                }
                else -> {
                    emptyString()
                }
            }
        }
    }

    private fun BridgeSendContract.View.handleUpdateArbiterFee(
        token: SendToken.Bridge,
        sendFee: SendFee?,
        isInputEmpty: Boolean
    ) {
        val bridgeFee = sendFee as? SendFee.Bridge
        val formattedArbiterFee = bridgeSendUiMapper.getArbiterFeesFormattedToken(
            token = token,
            bridgeFee = bridgeFee,
            isInputEmpty = isInputEmpty
        )
        val totalInUsd = bridgeFee?.fee
            ?.arbiterFee
            ?.toBridgeAmount()
            ?.fiatAmount
            .orZero()

        val isHighFees = totalInUsd >= MAX_FEE_AMOUNT
        val feeColor = if (isHighFees) R.color.text_rose else R.color.text_mountain
        setFeeColor(feeColor)
        showBottomFeeValue(
            TextViewCellModel.Raw(
                TextContainer(text = formattedArbiterFee),
                textColor = feeColor
            )
        )
    }

    override fun onTokenClicked() {
        sendBridgesAnalytics.logTokenSelectionClicked()
        launch {
            val token = currentState.lastStaticState.bridgeToken?.token
            val tokens = bridgeInteractor.supportedSendTokens()
            view?.showTokenSelection(supportedTokens = tokens, selectedToken = token)
        }
    }

    override fun updateToken(newToken: Token.Active) {
        sendBridgesAnalytics.logTokenChanged(newToken.tokenSymbol)
        stateMachine.newAction(SendFeatureAction.NewToken(SendToken.Bridge(newToken)))
    }

    override fun switchCurrencyMode() {
        view?.updateInputValue(calculationMode.switchAndUpdateInputAmount(), true)
        view?.handleUpdateTotal(currentState.lastStaticState.bridgeFee)
    }

    override fun updateInputAmount(amount: String) {
        val tokeSymbol = currentState.lastStaticState.bridgeToken?.token?.tokenSymbol.orEmpty()
        sendBridgesAnalytics.logTokenAmountChanged(tokeSymbol, amount)
        calculationMode.updateInputAmount(amount)
        val currentAmount = calculationMode.getCurrentAmount()
        if (currentAmount.isZero()) {
            stateMachine.newAction(SendFeatureAction.ZeroAmount)
        } else {
            stateMachine.newAction(SendFeatureAction.AmountChange(currentAmount))
        }
    }

    override fun onMaxButtonClicked() {
        val feeTotalInToken = getFeeTotalInToken()
        val token = currentState.lastStaticState.bridgeToken?.token ?: return
        stateMachine.newAction(SendFeatureAction.MaxAmount)
        calculationMode.updateTokenAmount(token.total - feeTotalInToken)
        view?.updateInputValue(calculationMode.formatInputAmount, true)
        val message = resources.getString(R.string.send_using_max_amount, token.tokenSymbol)
        view?.showToast(TextContainer.Raw(message))
    }

    private fun getFeeTotalInToken(): BigDecimal {
        return getFeeList().sumOf { it.amountInToken }
    }

    private fun getFeeTotalInUsd(): BigDecimal {
        return getFeeList().sumOf { it.amountInUsd.toBigDecimalOrZero() }
    }

    private fun getFeeList(bridgeSendFee: SendFee.Bridge? = null): List<BridgeFee> {
        val bridgeFee = bridgeSendFee ?: currentState.lastStaticState.bridgeFee
        return bridgeFee?.fee.getFeeList()
    }

    override fun onFeeInfoClicked() {
        val fees = currentState.lastStaticState.bridgeFee?.fee
        if (calculationMode.isCurrentInputEmpty() && fees == null) {
            view?.showFreeTransactionsInfo()
        } else {
            val feeDetails = getFeeDetails()
            view?.showTransactionDetails(feeDetails)
        }
        sendBridgesAnalytics.logFreeTransactionsClicked()
    }

    override fun checkInternetConnection() {
        if (!isInternetConnectionEnabled()) {
            view?.showUiKitSnackBar(
                message = resources.getString(R.string.error_no_internet_message),
                actionButtonResId = R.string.common_hide
            )
            view?.restoreSlider()
            return
        }

        view?.showSliderCompleteAnimation()
    }

    override fun finishFeature() {
        stateMachine.finishWork()
    }

    override fun send() {
        val token = currentState.lastStaticState.bridgeToken?.token ?: error("Token cannot be null!")

        val currentAmount = calculationMode.getCurrentAmount()
        val currentAmountUsd = calculationMode.getCurrentAmountUsd()

        val fee = currentState.lastStaticState.bridgeFee?.fee?.let { fees ->
            val feeList = listOf(
                fees.networkFeeInToken,
                fees.messageAccountRentInToken,
                fees.bridgeFeeInToken,
                fees.arbiterFee
            )
            feeList.sumOf { it.amountInUsd?.toBigDecimal() ?: BigDecimal.ZERO }
        } ?: BigDecimal.ZERO
        logSendClicked(
            token = token,
            amountInToken = currentAmount.toPlainString(),
            amountInUsd = currentAmountUsd.orZero().toPlainString(),
            fee = fee.toPlainString()
        )

        val sendTransaction = (currentState as? SendState.Static.ReadyToSend)?.sendTransaction ?: return
        val transactionId = sendTransaction.message ?: return

        appScope.launch {
            val transactionDate = Date()
            try {
                val feeDetails = getFeeDetails()
                val progressDetails = bridgeSendUiMapper.prepareShowProgress(
                    iconUrl = token.iconUrl.orEmpty(),
                    amountTokens = "${currentAmount.toPlainString()} ${token.tokenSymbol}",
                    amountUsd = currentAmountUsd?.asNegativeUsdTransaction(),
                    recipient = recipientAddress.nicknameOrAddress(),
                    feeDetails = feeDetails
                )

                view?.showProgressDialog(transactionId, progressDetails)
                val progressState = TransactionState.Progress(
                    description = R.string.bridge_send_transaction_description_progress
                )
                transactionManager.emitTransactionState(transactionId, progressState)

                val result = bridgeInteractor.sendTransaction(
                    sendTransaction = sendTransaction,
                    token = token
                )
                userInteractor.addRecipient(recipientAddress, transactionDate)
            } catch (e: Throwable) {
                Timber.e(e, "Error sending the token via bridge")
                val message = e.getErrorMessage { res -> resources.getString(res) }
                transactionManager.emitTransactionState(transactionId, BridgeSendProgressState.Error(message))
                logSendErrorAlarm(e)
            }
        }
    }

    private fun getFeeDetails(): BridgeFeeDetails {
        val fees = currentState.lastStaticState.bridgeFee?.fee
        return bridgeSendUiMapper.makeBridgeFeeDetails(
            recipientAddress = recipientAddress.addressState.address,
            fees = fees
        )
    }

    private fun SearchResult.nicknameOrAddress(): String {
        return if (this is SearchResult.UsernameFound) formattedUsername
        else addressState.address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)
    }

    private fun isInternetConnectionEnabled(): Boolean =
        connectionStateProvider.hasConnection()

    private fun logSendClicked(
        token: Token.Active,
        amountInToken: String,
        amountInUsd: String,
        fee: String,
    ) {
        sendBridgesAnalytics.logSendConfirmButtonClicked(
            tokenSymbol = token.tokenSymbol,
            amountInToken = amountInToken,
            amountInUsd = amountInUsd,
            fee = fee,
        )
    }

    private fun logSendErrorAlarm(e: Throwable) {
        val lastStaticState = currentState.lastStaticState
        val token = lastStaticState.bridgeToken?.token ?: return
        val arbiterFeeAmount = lastStaticState.bridgeFee?.fee?.arbiterFee?.amountInToken?.toPlainString()
        alarmErrorsLogger.triggerBridgeSendAlarm(
            token = token,
            currency = calculationMode.getCurrencyMode().getCurrencyModeSymbol(),
            sendAmount = calculationMode.getCurrentAmount().toPlainString(),
            arbiterFeeAmount = arbiterFeeAmount.orEmpty(),
            recipientEthPubkey = recipientAddress.addressState.address,
            error = e
        )
    }
}
