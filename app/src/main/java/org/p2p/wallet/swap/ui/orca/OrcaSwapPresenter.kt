package org.p2p.wallet.swap.ui.orca

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.settings.interactor.SettingsInteractor
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
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.divideSafe
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.isMoreThan
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.isZero
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
    private val settingsInteractor: SettingsInteractor
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
    private var slippage: Slippage = Slippage.MEDIUM

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
                view?.showFullScreenLoading(false)
            }
        }
    }

    override fun loadTokensForSourceSelection() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val result = tokens.filter { token -> !token.isZero }
            view?.showSourceSelection(result)
        }
    }

    override fun loadTokensForDestinationSelection() {
        launch {
            try {
                val orcaTokens = orcaPoolInteractor.findPossibleDestinations(sourceToken.mintAddress)
                view?.showDestinationSelection(orcaTokens)
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
        val amount = sourceToken.total.scaleLong().toString()
        setSourceAmount(amount)
        view?.showNewAmount(amount)
    }

    override fun setSourceAmount(amount: String) {
        sourceAmount = amount

        if (!this::sourceToken.isInitialized) return

        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = sourceToken.usdRateOrZero.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(sourceToken.total)
        val availableColor = if (isMoreThanBalance) R.color.systemErrorMain else R.color.textIconPrimary

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
            val tokens = listOf(sol, sourceToken)

            view?.showSwapSettings(slippage, tokens, swapInteractor.getFeePayerToken())
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
        destinationAmount = ""
        view?.showTotal(null)
        view?.showNewAmount(sourceAmount)

        calculateData(sourceToken, destinationToken!!)
    }

    override fun swapOrConfirm() {
        val isConfirmationRequired = settingsInteractor.isBiometricsConfirmationEnabled()
        if (isConfirmationRequired) {
            val data = SwapConfirmData(
                sourceToken = sourceToken,
                destinationToken = destinationToken!!,
                sourceAmount = sourceAmount,
                sourceAmountUsd = sourceAmount.toBigDecimalOrZero().toUsd(sourceToken)?.toString(),
                destinationAmount = destinationAmount,
                destinationAmountUsd = destinationAmount.toBigDecimalOrZero().toUsd(destinationToken!!)?.toString()
            )
            view?.showBiometricConfirmationPrompt(data)
        } else {
            swap()
        }
    }

    /**
     * Sometimes swap operation is being executed too long
     * Therefore, launching swap operation is launching in app scope, so user could move inside the app
     * w/o interrupting swap operation
     * */
    override fun swap() {
        val pair = bestPoolPair ?: return
        val destination = destinationToken ?: return

        appScope.launch {
            try {
                val subTitle =
                    "$sourceAmount ${sourceToken.tokenSymbol} → $destinationAmount ${destination.tokenSymbol}"
                val progress = ShowProgress(
                    title = R.string.swap_being_processed,
                    subTitle = subTitle,
                    transactionId = ""
                )
                view?.showProgressDialog(progress)
                val data = swapInteractor.swap(
                    fromToken = sourceToken,
                    toToken = destination,
                    bestPoolsPair = pair,
                    amount = sourceAmount.toDoubleOrNull() ?: 0.toDouble(),
                    slippage = slippage.doubleValue
                )

                when (data) {
                    is OrcaSwapResult.Finished ->
                        buildTransaction(data.transactionId, data.destinationAddress)
                    is OrcaSwapResult.InvalidInfoOrPair,
                    is OrcaSwapResult.InvalidPool ->
                        view?.showErrorMessage(R.string.error_general_message)
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error swapping tokens")
                view?.showErrorMessage(e)
            } finally {
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

        val pair = bestPoolPair ?: return

        fees = swapInteractor.calculateFeeAndNeededTopUpAmountForSwapping(
            sourceToken = source,
            destination = destination,
            swapPools = pair
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
        destinationAmount = estimatedOutputAmount.fromLamports(destination.decimals).scaleLong().toString()

        val minReceive = pair.getMinimumAmountOut(inputAmount, slippage.doubleValue) ?: return
        val minReceiveResult = minReceive.fromLamports(destination.decimals).scaleLong()

        val totalUsd = sourceAmount.toBigDecimalOrZero().multiply(source.usdRateOrZero)

        val receiveAtLeast = "${minReceiveResult.toPlainString()} ${destination.tokenSymbol}"
        val receiveAtLeastUsd = destination.usdRate?.let { minReceiveResult.multiply(it) }

        val data = SwapTotal(
            destinationAmount = destinationAmount,
            total = "$sourceAmount ${source.tokenSymbol}",
            totalUsd = totalUsd.scaleShort().toPlainString(),
            fee = fees?.accountCreationFee,
            approxFeeUsd = fees?.approxFeeUsd.orEmpty(),
            receiveAtLeast = receiveAtLeast,
            receiveAtLeastUsd = receiveAtLeastUsd?.toPlainString()
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

        val inputPrice = inputAmount.divideSafe(estimatedOutputAmount)
        val inputPriceUsd = source.usdRate?.let { inputPrice.multiply(it) }
        val outputPrice = estimatedOutputAmount.divideSafe(inputAmount)
        val outputPriceUsd = destination.usdRate?.let { outputPrice.multiply(it) }
        val priceData = SwapPrice(
            sourceSymbol = source.tokenSymbol,
            destinationSymbol = destination.tokenSymbol,
            sourcePrice = "${inputPrice.scaleMedium().toPlainString()} ${source.tokenSymbol}",
            destinationPrice = "${outputPrice.scaleMedium().toPlainString()} ${destination.tokenSymbol}",
            sourcePriceInUsd = inputPriceUsd?.scaleShort()?.toPlainString(),
            destinationPriceInUsd = outputPriceUsd?.scaleShort()?.toPlainString()
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
        transactionId: String = "",
        destinationAddress: String
    ) {
        val destinationToken = destinationToken ?: return
        val amountA = sourceAmount.toBigDecimalOrZero()
        val amountB = destinationAmount.toBigDecimalOrZero()
        val transaction = HistoryTransaction.Swap(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = 0, // fixme: find block number
            sourceAddress = sourceToken.publicKey,
            destinationAddress = destinationAddress,
            fee = BigInteger.ZERO,
            amountA = amountA,
            amountB = amountB,
            amountSentInUsd = amountA.toUsd(sourceToken),
            amountReceivedInUsd = amountB.toUsd(destinationToken.usdRate),
            sourceSymbol = sourceToken.tokenSymbol,
            sourceIconUrl = sourceToken.iconUrl.orEmpty(),
            destinationSymbol = destinationToken.tokenSymbol,
            destinationIconUrl = destinationToken.iconUrl.orEmpty()
        )
        view?.showTransactionDetails(transaction)
        val fromSymbol = transaction.sourceSymbol
        val toSymbol = transaction.destinationSymbol
        view?.showTransactionStatusMessage(fromSymbol, toSymbol, isSuccess = true)
    }
}