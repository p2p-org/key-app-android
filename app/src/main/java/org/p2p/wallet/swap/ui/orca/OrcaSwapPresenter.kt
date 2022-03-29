package org.p2p.wallet.swap.ui.orca

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.swap.analytics.SwapAnalytics
import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.SwapConfirmData
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getMinimumAmountOut
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getOutputAmount
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaSettingsResult
import org.p2p.wallet.swap.model.orca.OrcaSwapResult
import org.p2p.wallet.swap.model.orca.SwapFee
import org.p2p.wallet.swap.model.orca.SwapPrice
import org.p2p.wallet.swap.model.orca.SwapTotal
import org.p2p.wallet.transaction.TransactionManager
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.AmountUtils
import org.p2p.wallet.utils.divideSafe
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.isMoreThan
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.orZero
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.toBigDecimalOrZero
import org.p2p.wallet.utils.toLamports
import org.p2p.wallet.utils.toUsd
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.Delegates

// TODO: Refactor, make simpler
class OrcaSwapPresenter(
    private val initialToken: Token.Active?,
    private val appScope: AppScope,
    private val userInteractor: UserInteractor,
    private val swapInteractor: OrcaSwapInteractor,
    private val orcaPoolInteractor: OrcaPoolInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val browseAnalytics: BrowseAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val swapAnalytics: SwapAnalytics,
    private val transactionManager: TransactionManager
) : BasePresenter<OrcaSwapContract.View>(), OrcaSwapContract.Presenter {

    companion object {
        private const val TAG_SWAP = "SWAP_STATE"
    }

    private val poolPairs = mutableListOf<OrcaPoolsPair>()

    private lateinit var sourceToken: Token.Active

    private var destinationToken: Token? by Delegates.observable(null) { _, _, newValue ->
        view?.showDestinationToken(newValue)
    }

    private var bestPoolPair: OrcaPoolsPair? = null

    private var sourceAmount: String = "0"
    private var destinationAmount: String = "0"

    private var fees: SwapFee? = null

    private var aroundValue: BigDecimal = BigDecimal.ZERO
    private var slippage: Slippage = Slippage.Percent
    private var isMaxClicked: Boolean = false

    private var calculationJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            try {
                val userTokens = userInteractor.getUserTokens()
                val sol = userTokens.firstOrNull { it.isSOL } ?: throw IllegalStateException("No SOL account found")

                val token = initialToken ?: sol

                setSourceToken(token)
                view?.showSlippage(slippage)

                swapInteractor.initialize(sol)
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
        setSourceToken(newToken)
        clearDestination()
        updateButtonState(newToken)
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken
        calculateData(sourceToken, newToken)
    }

    override fun setNewSettings(settingsResult: OrcaSettingsResult) {
        this.slippage = settingsResult.newSlippage
        view?.showSlippage(this.slippage)

        destinationToken?.let {
            /* If pool is not null, then destination token is not null as well */
            calculateAmount(sourceToken, it)
        }

        swapInteractor.setFeePayerToken(settingsResult.newFeePayerToken)
        view?.showFeePayerToken(settingsResult.newFeePayerToken.tokenSymbol)
    }

    override fun calculateAvailableAmount() {
        val amount = AmountUtils.format(sourceToken.total.scaleLong())
        setSourceAmount(amount)
        view?.showNewAmount(amount)
        isMaxClicked = true
    }

    override fun setSourceAmount(amount: String) {
        sourceAmount = amount

        if (!this::sourceToken.isInitialized) return

        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = sourceToken.usdRateOrZero.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(sourceToken.total)
        val availableColor = if (isMoreThanBalance) R.color.systemErrorMain else R.color.textIconSecondary

        view?.setAvailableTextColor(availableColor)
        view?.showAroundValue(aroundValue)

        destinationToken?.let {
            calculationJob?.cancel()
            calculationJob = launch {
                /* If pool is not null, then destination token is not null as well */
                calculateAmount(sourceToken, it)

                /* Fee is being calculated including entered amount, thus calculating fee if entered amount changed */
                calculateFees(sourceToken, it)

                calculateRates(sourceToken, it)
            }
        }
    }

    override fun loadDataForSettings() {
        launch {
            val sol = userInteractor.getUserTokens().find { it.isSOL } ?: return@launch
            val tokens = mutableListOf(sol).apply {
                if (!sourceToken.isSOL) {
                    add(sourceToken)
                }
            }
            view?.showSwapSettings(slippage, tokens, swapInteractor.getFeePayerToken())
            val feeSource = if (sourceToken.isSOL) SwapAnalytics.FeeSource.SOL else SwapAnalytics.FeeSource.OTHER
            // TODO determine priceS lipaceExact
            swapAnalytics.logSwapShowingSettings(
                priceSlippage = slippage.doubleValue,
                priceSlippageExact = false,
                feesSource = feeSource,
                swapSettingsSource = SwapAnalytics.SwapSettingsSource.ICON
            )
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
        view?.showTotal(null)
        view?.showNewAmount(sourceAmount)
        swapAnalytics.logSwapReversing(destinationToken?.tokenSymbol.orEmpty())
        calculateData(sourceToken, destinationToken!!)
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
        view?.close()
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

        appScope.launch {
            try {
                swapAnalytics.logSwapStarted(
                    tokenAName = sourceToken.tokenSymbol,
                    tokenBName = destinationToken?.tokenSymbol.orEmpty(),
                    swapSum = sourceAmount,
                    isSwapMax = isMaxClicked,
                    swapUsd = sourceAmount.toBigDecimalOrZero().toUsd(sourceToken) ?: BigDecimal.ZERO,
                    priceSlippage = slippage.doubleValue,
                    feesSource = SwapAnalytics.FeeSource.getValueOf(sourceToken.tokenSymbol)
                )

                val sourceTokenSymbol = sourceToken.tokenSymbol
                val destinationTokenSymbol = destination.tokenSymbol
                val subTitle =
                    "$sourceAmount $sourceTokenSymbol → $destinationAmount $destinationTokenSymbol"
                val progress = ShowProgress(
                    title = R.string.swap_being_processed,
                    subTitle = subTitle,
                    transactionId = emptyString()
                )
                view?.showProgressDialog(progress)
                swapAnalytics.logSwapProcessShown()
                val data = swapInteractor.swap(
                    fromToken = sourceToken,
                    toToken = destination,
                    bestPoolsPair = pair,
                    amount = sourceAmount.toBigDecimalOrZero().toLamports(sourceToken.decimals),
                    slippage = slippage
                )

                when (data) {
                    is OrcaSwapResult.Finished -> {
                        swapAnalytics.logSwapCompleted(
                            tokenAName = sourceToken.tokenSymbol,
                            tokenBName = destinationToken?.tokenSymbol.orEmpty(),
                            swapSum = sourceAmount,
                            isSwapMax = isMaxClicked,
                            swapUsd = sourceAmount.toBigDecimalOrZero().toUsd(sourceToken) ?: BigDecimal.ZERO,
                            priceSlippage = slippage.doubleValue,
                            feesSource = SwapAnalytics.FeeSource.getValueOf(sourceToken.tokenSymbol)
                        )

                        val state = TransactionState.SwapSuccess(
                            transaction = buildTransaction(
                                destination = destination,
                                transactionId = data.transactionId,
                                destinationAddress = data.destinationAddress
                            ),
                            fromToken = sourceTokenSymbol,
                            toToken = destinationTokenSymbol
                        )
                        transactionManager.emitTransactionState(state)
                    }
                    is OrcaSwapResult.InvalidInfoOrPair, is OrcaSwapResult.InvalidPool -> {
                        view?.showErrorMessage(R.string.error_general_message)
                    }
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error swapping tokens")
                view?.showErrorMessage(e)
                view?.showProgressDialog(null)
            }
        }
    }

    private fun calculateData(source: Token.Active, destination: Token) {
        launch {
            view?.showButtonText(R.string.swap_searching_swap_pair)
            searchTradablePairs(source, destination)
            view?.showButtonText(R.string.swap_calculating_fees)
            calculateAmount(source, destination)
            calculateFees(source, destination)
            calculateRates(source, destination)
        }
    }

    private suspend fun searchTradablePairs(source: Token.Active, destination: Token) {
        try {
            Timber.tag(TAG_SWAP).d("Searching pair for ${source.mintAddress} / ${destination.mintAddress}")
            val pairs = orcaPoolInteractor.getTradablePoolsPairs(source.mintAddress, destination.mintAddress)
            Timber.tag(TAG_SWAP).d("Loaded all tradable pool pairs. Size: ${pairs.size}")
            poolPairs.clear()
            poolPairs.addAll(pairs)
        } catch (e: Throwable) {
            Timber.e(e, "Error occurred while getting tradable pool pairs")
        }
    }

    private suspend fun calculateFees(source: Token.Active, destination: Token) {
        val enteredAmount = sourceAmount.toBigDecimalOrZero()
        if (enteredAmount.isZero()) {
            view?.showTotal(null)
            return
        }

        fees = swapInteractor.calculateFeeAndNeededTopUpAmountForSwapping(
            sourceToken = source,
            destination = destination
        )

        view?.showFees(fees)
    }

    private fun calculateAmount(source: Token.Active, destination: Token) {
        val inputAmount = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals)

        val pair = orcaPoolInteractor.findBestPoolsPairForInputAmount(inputAmount, poolPairs)
        if (pair.isNullOrEmpty()) {
            Timber.tag(TAG_SWAP).d("Best pair is empty")
            updateButtonState(source)
            return
        }

        bestPoolPair = pair

        val deprecatedValues = pair.joinToString { "${it.tokenAName} -> ${it.tokenBName} (${it.deprecated})" }
        Timber.tag(TAG_SWAP).d("Best pair found, deprecation values: $deprecatedValues")
        val estimatedOutputAmount = pair.getOutputAmount(inputAmount) ?: return
        destinationAmount = AmountUtils.format(estimatedOutputAmount.fromLamports(destination.decimals).scaleLong())

        val minReceive = pair.getMinimumAmountOut(inputAmount, slippage.doubleValue) ?: return
        val minReceiveResult = minReceive.fromLamports(destination.decimals).scaleLong()

        val totalUsd = sourceAmount.toBigDecimalOrZero().multiply(source.usdRateOrZero)

        val receiveAtLeast = "${AmountUtils.format(minReceiveResult)} ${destination.tokenSymbol}"
        val receiveAtLeastUsd = destination.usdRate?.let { minReceiveResult.multiply(it) }

        val data = SwapTotal(
            destinationAmount = destinationAmount,
            total = "${AmountUtils.format(sourceAmount.toBigDecimalOrZero())} ${source.tokenSymbol}",
            totalUsd = AmountUtils.format(totalUsd.scaleShort()),
            fee = fees?.transactionFeeString,
            approxFeeUsd = fees?.approxFeeUsd.orEmpty(),
            receiveAtLeast = receiveAtLeast,
            receiveAtLeastUsd = receiveAtLeastUsd?.let { AmountUtils.format(it) }
        )
        view?.showTotal(data)

        val feePayerToken = swapInteractor.getFeePayerToken()
        view?.showFeePayerToken(feePayerToken.tokenSymbol)
        updateButtonState(source)
    }

    private fun calculateRates(source: Token.Active, destination: Token) {
        val pair = bestPoolPair ?: return
        Timber.tag(TAG_SWAP).d("Calculating rates")

        val inputAmount = sourceAmount.toBigDecimalOrNull() ?: return
        val inputAmountBigInteger = inputAmount.toLamports(source.decimals)
        val estimatedOutputAmount = pair
            .getOutputAmount(inputAmountBigInteger)
            ?.fromLamports(destination.decimals) ?: return

        val inputPrice = inputAmount.divideSafe(estimatedOutputAmount).scaleMedium()
        val inputPriceUsd = source.usdRate?.let { inputPrice.multiply(it) }
        val outputPrice = estimatedOutputAmount.divideSafe(inputAmount).scaleMedium()
        val outputPriceUsd = destination.usdRate?.let { outputPrice.multiply(it) }
        val priceData = SwapPrice(
            sourceSymbol = source.tokenSymbol,
            destinationSymbol = destination.tokenSymbol,
            sourcePrice = "${AmountUtils.format(inputPrice)} ${source.tokenSymbol}",
            destinationPrice = "${AmountUtils.format(outputPrice)} ${destination.tokenSymbol}",
            sourcePriceInUsd = inputPriceUsd?.scaleShort()?.let { AmountUtils.format(it) },
            destinationPriceInUsd = outputPriceUsd?.scaleShort()?.let { AmountUtils.format(it) }
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

    private fun updateButtonState(sourceToken: Token.Active) {
        val isPoolPairEmpty = poolPairs.isEmpty()
        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        val isMoreThanBalance = decimalAmount.isMoreThan(sourceToken.total)
        val isValidAmount = decimalAmount.isNotZero()

        when {
            isPoolPairEmpty -> view?.showButtonText(R.string.swap_cannot_swap_these_tokens)
            isMoreThanBalance -> view?.showButtonText(R.string.swap_funds_not_enough)
            decimalAmount.isZero() -> view?.showButtonText(R.string.main_enter_the_amount)
            destinationToken == null -> view?.showButtonText(R.string.swap_choose_the_destination)
            else -> {
                view?.showButtonText(
                    textRes = R.string.swap_format,
                    iconRes = R.drawable.ic_swap_simple,
                    sourceToken.tokenSymbol,
                    destinationToken?.tokenSymbol.orEmpty()
                )
            }
        }

        val isEnabled = isValidAmount && !isMoreThanBalance && destinationToken != null && !isPoolPairEmpty
        view?.showButtonEnabled(isEnabled)
    }

    private fun buildTransaction(
        destination: Token,
        transactionId: String,
        destinationAddress: String
    ): HistoryTransaction {
        val amountA = sourceAmount.toBigDecimalOrZero()
        val amountB = destinationAmount.toBigDecimalOrZero()
        return HistoryTransaction.Swap(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = null,
            sourceAddress = sourceToken.publicKey,
            destinationAddress = destinationAddress,
            fee = BigInteger.ZERO,
            amountA = amountA,
            amountB = amountB,
            amountSentInUsd = amountA.toUsd(sourceToken),
            amountReceivedInUsd = amountB.toUsd(destination.usdRate),
            sourceSymbol = sourceToken.tokenSymbol,
            sourceIconUrl = sourceToken.iconUrl.orEmpty(),
            destinationSymbol = destination.tokenSymbol,
            destinationIconUrl = destination.iconUrl.orEmpty(),
            status = TransactionStatus.PENDING
        )
    }
}
