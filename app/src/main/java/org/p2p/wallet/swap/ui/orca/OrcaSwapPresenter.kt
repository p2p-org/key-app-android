package org.p2p.wallet.swap.ui.orca

import android.content.res.Resources
import org.p2p.core.token.Token
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.NO_ACTION
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.SELECT_FEE_PAYER
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.send.model.FeePayerState
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.swap.analytics.SwapAnalytics
import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import org.p2p.wallet.swap.model.FeeRelayerSwapFee
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.SwapButton
import org.p2p.wallet.swap.model.SwapConfirmData
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getMinimumAmountOut
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getOutputAmount
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaSettingsResult
import org.p2p.wallet.swap.model.orca.OrcaSwapResult
import org.p2p.wallet.swap.model.orca.SwapFee
import org.p2p.wallet.swap.model.orca.SwapPrice
import org.p2p.wallet.swap.model.orca.SwapTotal
import org.p2p.wallet.transaction.interactor.TransactionBuilderInteractor
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.divideSafe
import org.p2p.wallet.utils.emptyString
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.util.UUID
import kotlin.properties.Delegates.observable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG_SWAP = "SWAP_STATE"

// TODO: Refactor, make simpler
class OrcaSwapPresenter(
    private val resources: Resources,
    private val initialToken: Token.Active?,
    private val appScope: AppScope,
    private val userInteractor: UserInteractor,
    private val swapInteractor: OrcaSwapInteractor,
    private val orcaPoolInteractor: OrcaPoolInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val transactionBuilderInteractor: TransactionBuilderInteractor,
    private val browseAnalytics: BrowseAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val swapAnalytics: SwapAnalytics,
    private val transactionManager: TransactionManager
) : BasePresenter<OrcaSwapContract.View>(), OrcaSwapContract.Presenter {

    private var destinationToken: Token? by observable(null) { _, _, newValue ->
        newValue?.let { swapAnalytics.logSwapChangingTokenBNew(it.tokenSymbol) }

        view?.showDestinationToken(newValue)
    }

    private val poolPairs = mutableListOf<OrcaPoolsPair>()

    private lateinit var sourceToken: Token.Active

    private var bestPoolPair: OrcaPoolsPair? = null
        set(value) {
            field = value
            value?.also { showDebugBestSwapPairRoute(it) }
        }

    private var solToken: Token.Active? = null

    private var swapFee: SwapFee? = null

    private var sourceAmount: String = "0"
    private var destinationAmount: String = "0"

    private var slippage: Slippage = Slippage.TopUpSlippage
    private var isMaxClicked: Boolean = false

    private var validationJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            try {
                /*
                * We should find SOL anyway because SOL is needed for Selection Mechanism
                * */
                solToken = userInteractor.getUserTokens().find { it.isSOL }

                val token = initialToken
                    ?: solToken
                    ?: error("No SOL account found")

                setSourceToken(token)
                view?.showSlippage(slippage)

                swapInteractor.initialize(token)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading all data for swap")
                view?.showErrorMessage(e)
            } finally {
                swapAnalytics.logSwapViewed(analyticsInteractor.getPreviousScreenName())
                view?.showFullScreenLoading(false)
            }
        }
    }

    override fun loadTokensForSourceSelection() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val result = tokens.filter { token -> !token.isZero }
            browseAnalytics.logTokenListViewed(
                lastScreenName = analyticsInteractor.getPreviousScreenName(),
                tokenListLocation = BrowseAnalytics.TokenListLocation.TOKEN_A
            )
            view?.showSourceSelection(result)
            swapAnalytics.logSwapChangingTokenA(sourceToken.tokenSymbol)
        }
    }

    override fun loadTokensForDestinationSelection() {
        launch {
            try {
                val orcaTokens = orcaPoolInteractor.findPossibleDestinations(sourceToken.mintAddress)
                browseAnalytics.logTokenListViewed(
                    lastScreenName = analyticsInteractor.getPreviousScreenName(),
                    tokenListLocation = BrowseAnalytics.TokenListLocation.TOKEN_B
                )
                view?.showDestinationSelection(orcaTokens)
                swapAnalytics.logSwapChangingTokenB(destinationToken?.tokenName.orEmpty())
            } catch (e: Throwable) {
                Timber.e(e, "Error searching possible destinations")
                view?.showDestinationSelection(emptyList())
            }
        }
    }

    override fun setNewSourceToken(newToken: Token.Active) {
        swapAnalytics.logSwapChangingTokenANew(newToken.tokenSymbol)
        setSourceToken(newToken)
        clearDestination()
        updateButtonState()
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken
        calculateData(source = sourceToken, destination = newToken)
    }

    override fun setNewSettings(settingsResult: OrcaSettingsResult) {
        this.slippage = settingsResult.newSlippage
        view?.showSlippage(this.slippage)

        swapInteractor.setFeePayerToken(settingsResult.newFeePayerToken)
        view?.showFeePayerToken(settingsResult.newFeePayerToken.tokenSymbol)

        val destination = destinationToken ?: return
        findValidFeePayer(NO_ACTION, destination, desiredFeePayerToken = settingsResult.newFeePayerToken)
    }

    override fun fillMaxAmount() {
        view?.showNewSourceAmount(sourceAmount)
        val amount = sourceToken.total.formatToken()
        setSourceAmount(amount = amount, isMaxClicked = true)
    }

    override fun setSourceAmount(amount: String, isMaxClicked: Boolean) {
        sourceAmount = amount

        if (!this::sourceToken.isInitialized) return

        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        val aroundValue = sourceToken.usdRateOrZero.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(sourceToken.total)
        val availableColor = if (isMoreThanBalance) R.color.systemErrorMain else R.color.textIconSecondary

        view?.setTotalAmountTextColor(availableColor)
        view?.showAroundValue(aroundValue)

        this.isMaxClicked = isMaxClicked

        if (destinationToken == null) {
            updateButtonState()
        }

        val destination = destinationToken ?: return

        calculateBestPair()
        val strategy = if (isMaxClicked) CORRECT_AMOUNT else SELECT_FEE_PAYER
        findValidFeePayer(strategy, destination, desiredFeePayerToken = sourceToken)
    }

    override fun loadDataForSettings() {
        launch {
            val sol = userInteractor.getUserTokens().find { it.isSOL } ?: return@launch
            val tokens = mutableListOf(sol).apply {
                if (!sourceToken.isSOL) add(sourceToken)
            }
            view?.showSwapSettings(slippage, tokens, swapInteractor.getFeePayerToken())
            val feeSource = if (sourceToken.isSOL) SwapAnalytics.FeeSource.SOL else SwapAnalytics.FeeSource.OTHER
            logSwapSettingsOpened(feeSource)
        }
    }

    override fun reverseTokens() {
        if (destinationToken == null || destinationToken is Token.Other) return

        /* reversing tokens */
        val source = sourceToken
        val destination = destinationToken

        sourceToken = destination as Token.Active
        destinationToken = source
        view?.showSourceToken(sourceToken)

        /* reversing amounts */
        sourceAmount = destinationAmount
        destinationAmount = emptyString()
        calculateTotal(destinationToken!!, fee = null)
        view?.showNewSourceAmount(sourceAmount)
        swapAnalytics.logSwapReversing(destinationToken?.tokenSymbol.orEmpty())
        calculateData(source = sourceToken, destination = destinationToken!!)
    }

    override fun onFeeLimitsClicked() {
        launch {
            try {
                val freeTransactionsInfo = swapInteractor.getFreeTransactionsInfo()
                view?.showFeeLimitsDialog(freeTransactionsInfo.maxUsage, freeTransactionsInfo.remaining)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading free transactions info")
            }
        }
    }

    override fun onBackPressed() {
        logBackPressed()
        view?.closeScreen()
    }

    override fun swapOrConfirm() {
        val destination = destinationToken ?: throw IllegalStateException("Destination is null")
        val isConfirmationRequired = settingsInteractor.isBiometricsConfirmationEnabled()
        if (isConfirmationRequired) {
            val sourceAmountUsd = sourceAmount.toBigDecimalOrZero().toUsd(sourceToken).orZero()
            val destinationAmountUsd = destinationAmount.toBigDecimalOrZero().toUsd(destination).orZero()
            val data = SwapConfirmData(
                sourceToken = sourceToken,
                destinationToken = destinationToken!!,
                sourceAmount = sourceAmount,
                sourceAmountUsd = sourceAmountUsd.toString(),
                destinationAmount = destinationAmount,
                destinationAmountUsd = destinationAmountUsd.toString()
            )
            logSwapConfirmed()
            view?.showBiometricConfirmationPrompt(data)
        } else {
            swap()
            swapAnalytics.logSwapReversing(destinationToken?.tokenSymbol.orEmpty())
        }
    }

    /**
     * Sometimes swap operation is being executed too long
     * Therefore, launching swap operation is in app scope, so user could move to other screens inside the app
     * w/o interrupting swap operation
     * */
    override fun swap() {
        val pair = bestPoolPair ?: return
        val destination = destinationToken ?: throw IllegalStateException("Destination is null")
        val transactionId = UUID.randomUUID().toString()
        appScope.launch {
            try {
                logSwapStarted()

                val sourceTokenSymbol = sourceToken.tokenSymbol
                val destinationTokenSymbol = destination.tokenSymbol
                val subTitle = "$sourceAmount $sourceTokenSymbol → $destinationAmount $destinationTokenSymbol"
                val progress = ShowProgress(
                    title = R.string.swap_being_processed,
                    subTitle = subTitle,
                    transactionId = emptyString()
                )
                view?.showProgressDialog(transactionId, progress)
                swapAnalytics.logSwapProcessShown()
                val result = swapInteractor.swap(
                    fromToken = sourceToken,
                    toToken = destination,
                    bestPoolsPair = pair,
                    amount = sourceAmount.toBigDecimalOrZero().toLamports(sourceToken.decimals),
                    slippage = slippage
                )

                when (result) {
                    is OrcaSwapResult.Finished ->
                        handleSwapResult(transactionId, destination, result, sourceTokenSymbol, destinationTokenSymbol)
                    is OrcaSwapResult.InvalidInfoOrPair,
                    is OrcaSwapResult.InvalidPool ->
                        view?.showErrorMessage(R.string.error_general_message)
                }
            } catch (serverError: ServerException) {
                val state = TransactionState.Error(serverError.getErrorMessage(resources).orEmpty())
                transactionManager.emitTransactionState(transactionId, state)
            } catch (error: Throwable) {
                showError(transactionId, error)
            }
        }
    }

    override fun cleanFields() {
        val initialToken = initialToken ?: solToken ?: return
        setNewSourceToken(initialToken)
        view?.showNewSourceAmount(emptyString())

        sourceAmount = "0"

        isMaxClicked = false
    }

    private fun findValidFeePayer(
        strategy: FeePayerSelectionStrategy,
        destination: Token,
        desiredFeePayerToken: Token.Active?
    ) {
        validationJob?.cancel()
        launch {
            try {
                /* Fee is being calculated including entered amount, thus calculating fee if entered amount changed */
                calculateFees(strategy, desiredFeePayerToken, destination)
            } catch (e: CancellationException) {
                Timber.w("Cancelled fee payer validation")
            } catch (e: Throwable) {
                Timber.wtf(e, "Unexpected error during fee payer validation")
            }
        }.also { validationJob = it }
    }

    /**
     * This launches full recalculation
     * It's being called from the places:
     * - when destination token is selected (assuming source is selected as well)
     * - when user reversed tokens
     * */
    private fun calculateData(source: Token.Active, destination: Token) {
        launch {
            try {
                searchTradablePairs(source, destination)
                findValidFeePayer(CORRECT_AMOUNT, destination, desiredFeePayerToken = sourceToken)
            } catch (e: Throwable) {
                Timber.e("Error calculating data")
            }
        }
    }

    private suspend fun searchTradablePairs(source: Token.Active, destination: Token) {
        try {
            Timber.tag(TAG_SWAP).d("Searching pair for ${source.mintAddress} / ${destination.mintAddress}")
            val pairs = orcaPoolInteractor.getTradablePoolsPairs(source.mintAddress, destination.mintAddress)
            Timber.tag(TAG_SWAP).d("Loaded all tradable pool pairs. Size: ${pairs.size}")
            poolPairs.clear()
            poolPairs.addAll(pairs)

            calculateBestPair()
        } catch (e: Throwable) {
            Timber.e(e, "Error occurred while getting tradable pool pairs")
        }
    }

    private suspend fun recalculate(destination: Token) {
        /*
        * Optimized recalculation and UI update
        * */
        val newFeePayer = swapInteractor.getFeePayerToken()
        val fee = calculateFeeRelayerFee(sourceToken, destination, newFeePayer)
        if (fee == null) {
            swapFee = null
            view?.showFees(null)
            return
        }

        val swapFee = buildSwapFee(newFeePayer, destination, fee)
        showInsufficientFundsIfNeeded(sourceToken, swapFee)
        calculateTotal(destination, swapFee)
    }

    private suspend fun calculateFees(
        strategy: FeePayerSelectionStrategy,
        feePayerToken: Token.Active?,
        destination: Token
    ) {
        val enteredAmount = sourceAmount.toBigDecimalOrZero()
        if (enteredAmount.isZero()) {
            view?.showTotal(null)
            return
        }

        val feePayer = feePayerToken ?: swapInteractor.getFeePayerToken()

        val fee = calculateFeeRelayerFee(
            feePayerToken = feePayer,
            sourceToken = sourceToken,
            destination = destination
        )

        if (fee == null) {
            swapFee = null
            view?.showFees(null)
            return
        }

        showFeeDetails(
            sourceToken = sourceToken,
            destination = destination,
            fee = fee,
            feePayerToken = feePayer,
            strategy = strategy,
        )
    }

    private suspend inline fun calculateFeeRelayerFee(
        sourceToken: Token.Active,
        destination: Token,
        feePayerToken: Token.Active
    ): FeeRelayerSwapFee? {

        val fees = swapInteractor.calculateFeesForFeeRelayer(
            feePayerToken = feePayerToken,
            sourceToken = sourceToken,
            destination = destination
        )

        /*
         * Checking if fee is null
         * - Fee can be null for DIRECT swap if destination token is already created
         * - Fee can be null for TRANSITIVE swap if destination token is already created
         * */
        if (fees == null) {
            calculateTotal(destination, fee = null)
            return null
        }

        return fees
    }

    private suspend fun showFeeDetails(
        feePayerToken: Token.Active,
        sourceToken: Token.Active,
        destination: Token,
        fee: FeeRelayerSwapFee,
        strategy: FeePayerSelectionStrategy
    ) {
        val swapFee = buildSwapFee(feePayerToken, sourceToken, fee)

        if (strategy == NO_ACTION) {
            showInsufficientFundsIfNeeded(sourceToken, swapFee)
            calculateTotal(destination, swapFee)
        } else {
            validateAndSelectFeePayer(sourceToken, destination, swapFee, strategy)
        }
    }

    private fun buildSwapFee(
        newFeePayer: Token.Active,
        destination: Token,
        fee: FeeRelayerSwapFee
    ): SwapFee {

        return SwapFee(
            fee = fee,
            feePayerToken = newFeePayer,
            sourceToken = sourceToken,
            destination = destination,
            solToken = solToken
        ).also {
            swapFee = it
            view?.showFees(it)
        }
    }

    private fun showInsufficientFundsIfNeeded(source: Token.Active, fee: SwapFee) {
        val inputAmount = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals)
        val isEnoughToCoverExpenses = fee.isEnoughToCoverExpenses(
            sourceTokenTotal = source.totalInLamports,
            inputAmount = inputAmount
        )

        if (isEnoughToCoverExpenses) {
            view?.showFeePayerToken(feePayerTokenSymbol = fee.feePayerSymbol)
            view?.showSwapDetailsError(errorText = null)
        } else {
            val errorText = resources.getString(R.string.swap_insufficient_funds_format, fee.feePayerSymbol)
            view?.showSwapDetailsError(errorText = errorText)
        }
    }

    private suspend fun validateAndSelectFeePayer(
        sourceToken: Token.Active,
        destination: Token,
        fee: SwapFee,
        strategy: FeePayerSelectionStrategy
    ) {

        val inputAmount = sourceAmount.toBigDecimalOrZero().toLamports(sourceToken.decimals)
        val tokenTotal = sourceToken.total.toLamports(sourceToken.decimals)

        /*
       * Checking if fee payer is SOL, otherwise fee payer is already correctly set up
       * - if there is enough SPL balance to cover fee, setting the default fee payer as SPL token
       * - if there is not enough SPL/SOL balance to cover fee, trying to reduce input amount
       * - In other cases, switching to SOL
       * */
        when (val state = fee.calculateFeePayerState(strategy, tokenTotal, inputAmount)) {
            is FeePayerState.SwitchToSol -> {
                swapInteractor.switchFeePayerToSol(solToken)
                recalculate(destination)
            }
            is FeePayerState.SwitchToSpl -> {
                swapInteractor.switchFeePayerToSol(state.tokenToSwitch)
                recalculate(destination)
            }
            is FeePayerState.ReduceInputAmount -> {
                swapInteractor.setFeePayerToken(sourceToken)
                reduceInputAmount(state.maxAllowedAmount)
                recalculate(destination)
            }
        }
    }

    private fun calculateTotal(destination: Token, fee: SwapFee?) {
        val pair = bestPoolPair ?: return

        val inputAmount = sourceAmount.toBigDecimalOrZero()
        val inputAmountLamports = inputAmount.toLamports(sourceToken.decimals)

        val deprecatedValues = pair.joinToString { "${it.tokenAName} -> ${it.tokenBName} (${it.deprecated})" }
        Timber.tag(TAG_SWAP).d("Best pair found, deprecation values: $deprecatedValues")

        val estimatedOutputAmount = pair.getOutputAmount(inputAmountLamports) ?: return
        destinationAmount = estimatedOutputAmount.fromLamports(destination.decimals).formatToken()

        val minReceive = pair.getMinimumAmountOut(inputAmountLamports, slippage.doubleValue) ?: return
        val receiveAtLeast = minReceive.fromLamports(destination.decimals)

        val data = SwapTotal(
            destinationAmount = destinationAmount,
            fee = fee,
            inputAmount = inputAmount,
            destination = destination,
            sourceToken = sourceToken,
            receiveAtLeastDecimals = receiveAtLeast
        )
        view?.showTotal(data)

        updateButtonState()
    }

    private fun reduceInputAmount(maxAllowedAmount: BigInteger) {
        val newInputAmount = maxAllowedAmount.fromLamports(sourceToken.decimals).scaleLong()
        view?.showNewSourceAmount(newInputAmount.toString())

        sourceAmount = newInputAmount.toPlainString()

        updateMaxButtonVisibility(sourceToken)
    }

    private fun updateMaxButtonVisibility(token: Token.Active) {
        val totalAvailable = token.total.scaleLong()
        view?.setMaxButtonVisible(isVisible = sourceAmount != totalAvailable.toString())
    }

    private fun calculateBestPair() {
        val inputAmount = sourceAmount.toBigDecimalOrZero()
        val inputAmountLamports = inputAmount.toLamports(sourceToken.decimals)

        if (inputAmount.isZero() || poolPairs.isEmpty()) {
            updateButtonState()
            return
        }

        val pair = orcaPoolInteractor.findBestPoolsPairForInputAmount(inputAmountLamports, poolPairs)
        if (pair.isNullOrEmpty() || inputAmountLamports.isZero()) {
            Timber.tag(TAG_SWAP).d("Best pair is empty")
            updateButtonState()
            return
        }

        bestPoolPair = pair

        val destination = destinationToken ?: return
        calculateRates(pair, sourceToken, destination)
    }

    private fun calculateRates(poolsPair: OrcaPoolsPair, source: Token.Active, destination: Token) {
        Timber.tag(TAG_SWAP).d("Calculating rates")

        val inputAmount = sourceAmount.toBigDecimalOrNull() ?: return
        val inputAmountBigInteger = inputAmount.toLamports(source.decimals)
        val estimatedOutputAmount = poolsPair
            .getOutputAmount(inputAmountBigInteger)
            ?.fromLamports(destination.decimals) ?: return

        val inputPrice = inputAmount.divideSafe(estimatedOutputAmount).scaleMedium()
        val inputPriceUsd = source.rate?.let { inputPrice.multiply(it) }
        val outputPrice = estimatedOutputAmount.divideSafe(inputAmount).scaleMedium()
        val outputPriceUsd = destination.rate?.let { outputPrice.multiply(it) }
        val priceData = SwapPrice(
            sourceSymbol = source.tokenSymbol,
            destinationSymbol = destination.tokenSymbol,
            sourcePrice = "${inputPrice.formatToken()} ${source.tokenSymbol}",
            destinationPrice = "${outputPrice.formatToken()} ${destination.tokenSymbol}",
            sourcePriceInUsd = inputPriceUsd?.asUsd(),
            destinationPriceInUsd = outputPriceUsd?.asUsd()
        )
        view?.showPrice(priceData)
    }

    private fun setSourceToken(token: Token.Active) {
        destinationToken = null
        sourceToken = token
        view?.showSourceToken(sourceToken)
    }

    private fun clearDestination() {
        destinationToken = null
        destinationAmount = "0"
        view?.showPrice(null)

        view?.showTotal(null)
        view?.showButtonText(R.string.swap_choose_the_destination)
    }

    private fun updateButtonState() {
        val swapButton = SwapButton(
            bestPoolPair = bestPoolPair,
            sourceAmount = sourceAmount,
            sourceToken = sourceToken,
            destinationToken = destinationToken,
            swapFee = swapFee
        )

        when (val state = swapButton.state) {
            is SwapButton.State.Disabled -> {
                view?.showButtonText(state.textResId)
                view?.showButtonEnabled(isEnabled = false)
            }
            is SwapButton.State.Enabled -> {
                view?.showButtonText(state.textRes, state.iconRes, value = state.value)
                view?.showButtonEnabled(isEnabled = true)
            }
        }
    }

    private suspend fun handleSwapResult(
        transactionId: String,
        destination: Token,
        data: OrcaSwapResult.Finished,
        sourceTokenSymbol: String,
        destinationTokenSymbol: String
    ) {
        logSwapCompleted()
        val state = TransactionState.SwapSuccess(
            transaction = transactionBuilderInteractor.buildTransaction(
                source = sourceToken,
                destination = destination,
                sourceAmount = sourceAmount,
                destinationAmount = destinationAmount,
                transactionId = data.transactionId,
                destinationAddress = data.destinationAddress
            ),
            fromToken = sourceTokenSymbol,
            toToken = destinationTokenSymbol
        )
        transactionManager.emitTransactionState(transactionId, state)
    }

    private fun showError(transactionId: String, error: Throwable) {
        Timber.e(error, "Error swapping tokens")
        view?.showErrorMessage(error)
        view?.showProgressDialog(transactionId, null)
    }

    private fun showDebugBestSwapPairRoute(bestPairRoute: OrcaPoolsPair) {
        if (BuildConfig.DEBUG) {
            val routeAsString = bestPairRoute.joinToString { "${it.tokenAName} -> ${it.tokenBName}" }
            view?.showDebugSwapRoute(routeAsString.ifBlank { "No route found" })
        } else {
            view?.hideDebugSwapRoute()
        }
    }

    private fun logSwapStarted() {
        swapAnalytics.logSwapConfirmButtonClicked(
            tokenAName = sourceToken.tokenSymbol,
            tokenBName = destinationToken?.tokenSymbol.orEmpty(),
            swapSum = sourceAmount,
            isSwapMax = isMaxClicked,
            swapUsd = sourceAmount.toBigDecimalOrZero().toUsd(sourceToken) ?: BigDecimal.ZERO,
        )

        swapAnalytics.logSwapStarted(
            tokenAName = sourceToken.tokenSymbol,
            tokenBName = destinationToken?.tokenSymbol.orEmpty(),
            swapSum = sourceAmount,
            isSwapMax = isMaxClicked,
            swapUsd = sourceAmount.toBigDecimalOrZero().toUsd(sourceToken) ?: BigDecimal.ZERO,
            priceSlippage = slippage.doubleValue,
            feesSource = SwapAnalytics.FeeSource.getValueOf(sourceToken.tokenSymbol)
        )
    }

    private fun logSwapCompleted() {
        swapAnalytics.logSwapCompleted(
            tokenAName = sourceToken.tokenSymbol,
            tokenBName = destinationToken?.tokenSymbol.orEmpty(),
            swapSum = sourceAmount,
            isSwapMax = isMaxClicked,
            swapUsd = sourceAmount.toBigDecimalOrZero().toUsd(sourceToken) ?: BigDecimal.ZERO,
            priceSlippage = slippage.doubleValue,
            feesSource = SwapAnalytics.FeeSource.getValueOf(sourceToken.tokenSymbol)
        )
    }

    private fun logSwapSettingsOpened(feeSource: SwapAnalytics.FeeSource) {
        // TODO determine priceSlippageExact
        swapAnalytics.logSwapShowingSettings(
            priceSlippage = slippage.doubleValue,
            priceSlippageExact = false,
            feesSource = feeSource,
            swapSettingsSource = SwapAnalytics.SwapSettingsSource.ICON
        )
    }

    private fun logBackPressed() {
        // TODO determine [swapCurrency,priceSlippageExact,feeSource] param
        swapAnalytics.logSwapGoingBack(
            tokenAName = sourceToken.tokenSymbol,
            tokenBName = destinationToken?.tokenSymbol.orEmpty(),
            swapCurrency = sourceToken.tokenSymbol,
            swapSum = sourceAmount.toBigDecimalOrZero(),
            swapMax = isMaxClicked,
            swapUSD = sourceAmount.toBigDecimalOrZero().toUsd(sourceToken) ?: BigDecimal.ZERO,
            priceSlippage = slippage.doubleValue,
            priceSlippageExact = false,
            feesSource = SwapAnalytics.FeeSource.getValueOf(sourceToken.tokenSymbol)
        )
    }

    private fun logSwapConfirmed() {
        swapAnalytics.logSwapVerificationInvoked(AuthAnalytics.AuthType.BIOMETRIC)
        swapAnalytics.logSwapUserConfirmed(
            tokenAName = sourceToken.tokenSymbol,
            tokenBName = destinationToken?.tokenSymbol.orEmpty(),
            swapSum = sourceAmount,
            isSwapMax = isMaxClicked,
            swapUsd = sourceAmount.toBigDecimalOrZero().toUsd(sourceToken) ?: BigDecimal.ZERO,
            priceSlippage = slippage.doubleValue,
            feesSource = SwapAnalytics.FeeSource.getValueOf(sourceToken.tokenSymbol)
        )
    }
}
