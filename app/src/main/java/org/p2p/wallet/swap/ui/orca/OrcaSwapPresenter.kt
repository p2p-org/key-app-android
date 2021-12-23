package org.p2p.wallet.swap.ui.orca

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.ui.transaction.TransactionInfo
import org.p2p.wallet.swap.interactor.orca.OrcaAmountInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import org.p2p.wallet.swap.model.PriceData
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaAmountData
import org.p2p.wallet.swap.model.orca.OrcaFeeData
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getMinimumAmountOut
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getOutputAmount
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaSwapResult
import org.p2p.wallet.transaction.interactor.TransactionInteractor
import org.p2p.wallet.transaction.model.TransactionExecutionState
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.divideSafe
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.isMoreThan
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.toBigDecimalOrZero
import org.p2p.wallet.utils.toLamports
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
    private val amountInteractor: OrcaAmountInteractor,
    private val transactionInteractor: TransactionInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) : BasePresenter<OrcaSwapContract.View>(), OrcaSwapContract.Presenter {

    companion object {
        private const val TAG_SWAP_STATE = "SWAP_STATE"
    }

    private val poolPairs = mutableListOf<OrcaPoolsPair>()

    private lateinit var sourceToken: Token.Active

    private var destinationToken: Token? by Delegates.observable(null) { _, _, newValue ->
        view?.showDestinationToken(newValue)
    }

    private var bestPoolPair: OrcaPoolsPair? = null

    private var sourceAmount: String = "0"
    private var destinationAmount: String = "0"

    private var lamportsPerSignature: BigInteger = BigInteger.ZERO
    private var minRentExemption: BigInteger = BigInteger.ZERO

    private var aroundValue: BigDecimal = BigDecimal.ZERO
    private var slippage: Slippage = Slippage.MEDIUM

    private var calculationJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            try {
                val token = initialToken ?: userInteractor.getUserTokens().firstOrNull {
                    it.isSOL && it.publicKey == tokenKeyProvider.publicKey
                } ?: return@launch

                setSourceToken(token)
                view?.showSlippage(slippage)

                swapInteractor.load()

                lamportsPerSignature = amountInteractor.getLamportsPerSignature()
                minRentExemption = amountInteractor.getAccountMinForRentExemption()
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
            view?.openSourceSelection(result)
        }
    }

    override fun loadTokensForDestinationSelection() {
        launch {
            try {
                val orcaTokens = swapInteractor.findPossibleDestinations(sourceToken.mintAddress)
                view?.openDestinationSelection(orcaTokens)
            } catch (e: Throwable) {
                Timber.e(e, "Error searching possible destinations")
                view?.openDestinationSelection(emptyList())
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
        calculateData(newToken)
    }

    override fun setSlippage(slippage: Slippage) {
        this.slippage = slippage
        view?.showSlippage(this.slippage)

        destinationToken?.let {
            /* If pool is not null, then destination token is not null as well */
            calculateAmount(sourceToken, it)
        }
    }

    override fun loadDataForSwapSettings() {
        view?.openSwapSettings(slippage)
    }

    override fun loadSlippage() {
        view?.openSlippageDialog(slippage)
    }

    override fun feedAvailableValue() {
        view?.showNewAmount(sourceToken.total.scaleLong().toString())
    }

    override fun setSourceAmount(amount: String) {
        if (!this::sourceToken.isInitialized) return
        sourceAmount = amount

        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = sourceToken.usdRateOrZero.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(sourceToken.total)
        val availableColor = if (isMoreThanBalance) R.attr.colorAccentWarning else R.attr.colorAccentPrimary

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

    override fun reverseTokens() {
        if (destinationToken == null || destinationToken is Token.Other) return

        /* reversing tokens */
        val source = sourceToken
        sourceToken = destinationToken!! as Token.Active
        destinationToken = source
        view?.showSourceToken(sourceToken)

        /* reversing amounts */
        sourceAmount = destinationAmount
        destinationAmount = ""
        view?.showCalculations(null)

        /* This trigger recalculation */
        view?.showNewAmount(sourceAmount)
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
                view?.showLoading(true)
                val data = swapInteractor.swap(
                    fromWalletSymbol = sourceToken.tokenSymbol,
                    toWalletSymbol = destination.tokenSymbol,
                    fromWalletPubkey = sourceToken.publicKey,
                    toWalletPubkey = destination.publicKey,
                    bestPoolsPair = pair,
                    amount = sourceAmount.toDoubleOrNull() ?: 0.toDouble(),
                    slippage = slippage.doubleValue,
                    isSimulation = false
                )

                handleResult(data)
            } catch (e: Throwable) {
                Timber.e(e, "Error swapping tokens")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun calculateData(destination: Token) {
        launch {
            view?.showButtonText(R.string.swap_searching_swap_pair)
            searchTradablePairs(sourceToken, destination)
            view?.showButtonText(R.string.swap_calculating_fees)
            calculateAmount(sourceToken, destination)
            calculateFees(sourceToken, destination)
            calculateRates(sourceToken, destination)
        }
    }

    private suspend fun searchTradablePairs(source: Token.Active, destination: Token) {
        try {
            val pairs = swapInteractor.getTradablePoolsPairs(source.mintAddress, destination.mintAddress)
            Timber.tag(TAG_SWAP_STATE).d("Loaded all tradable pool pairs. Size: ${pairs.size}")
            poolPairs.clear()
            poolPairs.addAll(pairs)
        } catch (e: Throwable) {
            Timber.e(e, "Error occured while getting tradable pool pairs")
        }
    }

    private suspend fun calculateFees(source: Token.Active, destination: Token) {
        val enteredAmount = sourceAmount.toBigDecimalOrZero()
        if (enteredAmount.isZero()) {
            view?.hideCalculations()
            return
        }

        val pair = bestPoolPair ?: return

        val myWalletsMints = userInteractor.getUserTokens().map { it.mintAddress }
        val fees = swapInteractor.getFees(
            myWalletsMints = myWalletsMints,
            fromWalletPubkey = source.publicKey,
            toWalletPubkey = destination.publicKey,
            feeRelayerFeePayerPubkey = null,
            bestPoolsPair = pair,
            inputAmount = enteredAmount,
            slippage = slippage.doubleValue,
            lamportsPerSignature = lamportsPerSignature,
            minRentExempt = minRentExemption,
        )
        val networkFee = fees.first.fromLamports().scaleLong()
        val liquidityProviderFees = fees.second

        val liquidityProviderFee = if (pair.size == 1 && liquidityProviderFees.isNotEmpty()) {
            val fee = fees.second[0].fromLamports(destination.decimals).scaleLong()
            "$fee ${destination.tokenSymbol}"
        } else {
            val intermediaryPool = pair[0]
            val intermediaryTokenSymbol = intermediaryPool.tokenBName
            val intermediaryFee =
                liquidityProviderFees[0].fromLamports(intermediaryPool.tokenBBalance!!.decimals).scaleLong()

            val destinationPool = pair[1]
            val destinationTokenSymbol = destinationPool.tokenBName
            val destinationFee =
                liquidityProviderFees[1].fromLamports(destinationPool.tokenBBalance!!.decimals).scaleLong()
            "$intermediaryFee $intermediaryTokenSymbol + $destinationFee $destinationTokenSymbol"
        }

        val networkFeeResult = "$networkFee ${Token.SOL_SYMBOL}"

        // FIXME: pay network option implementation
        val feePaymentOption = Token.SOL_SYMBOL
        val data = OrcaFeeData(
            networkFee = networkFeeResult,
            liquidityProviderFee = liquidityProviderFee,
            paymentOption = feePaymentOption
        )
        view?.showFees(data)
    }

    private fun calculateAmount(source: Token.Active, destination: Token) {
        val inputAmount = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals)

        val pair = swapInteractor.findBestPoolsPairForInputAmount(inputAmount, poolPairs)
        if (pair.isNullOrEmpty()) {
            Timber.tag(TAG_SWAP_STATE).d("Best pair is empty")
            updateButtonState(source)
            return
        }

        bestPoolPair = pair

        val deprecatedValues = pair.joinToString { "${it.tokenAName} -> ${it.tokenBName} (${it.deprecated})" }
        Timber.tag(TAG_SWAP_STATE).d("Best pair found, deprecation values: $deprecatedValues")
        val estimatedOutputAmount = pair.getOutputAmount(inputAmount) ?: return
        destinationAmount = estimatedOutputAmount.fromLamports(destination.decimals).scaleLong().toString()

        val minReceive = pair.getMinimumAmountOut(inputAmount, slippage.doubleValue) ?: return
        val minReceiveResult = minReceive.fromLamports(destination.decimals).scaleLong()

        val data = OrcaAmountData(destinationAmount, minReceiveResult)
        view?.showCalculations(data)
        updateButtonState(source)
    }

    private fun calculateRates(source: Token.Active, destination: Token) {
        val pair = bestPoolPair ?: return
        Timber.tag(TAG_SWAP_STATE).d("Calculating rates")

        val inputAmount = sourceAmount.toBigDecimalOrNull() ?: return
        val inputAmountBigInteger = inputAmount.toLamports(source.decimals)
        val estimatedOutputAmount = pair
            .getOutputAmount(inputAmountBigInteger)
            ?.fromLamports(destination.decimals) ?: return

        val inputPrice = inputAmount.divideSafe(estimatedOutputAmount)
        val outputPrice = estimatedOutputAmount.divideSafe(inputAmount)
        val priceData = PriceData(
            inputPrice = inputPrice.scaleMedium().toString(),
            outputPrice = outputPrice.scaleMedium().toString(),
            inputSymbol = source.tokenSymbol,
            outputSymbol = destination.tokenSymbol
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
        view?.hidePrice()

        view?.hideCalculations()
        view?.showButtonText(R.string.main_select_token)
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
            destinationToken == null -> view?.showButtonText(R.string.main_select_token)
            else -> view?.showButtonText(R.string.main_swap_now)
        }

        val isEnabled = isValidAmount && !isMoreThanBalance && destinationToken != null && !isPoolPairEmpty
        view?.showButtonEnabled(isEnabled)
    }

    private fun handleResult(result: OrcaSwapResult) {
        when (result) {
            is OrcaSwapResult.Executing -> handleExecuting(result)
            is OrcaSwapResult.InvalidPool -> view?.showErrorMessage()
            is OrcaSwapResult.InvalidInfoOrPair -> view?.showErrorMessage()
        }
    }

    private fun handleExecuting(result: OrcaSwapResult.Executing) {
        val transactionFlow = transactionInteractor.getTransactionStateFlow(result.transactionId) ?: return

        launch {
            transactionFlow.collect { state ->
                when (state) {
                    is TransactionExecutionState.Executing -> view?.showLoading(true)
                    is TransactionExecutionState.Finished -> handleFinished(state)
                    is TransactionExecutionState.Failed -> handleFailed(state)
                    is TransactionExecutionState.Idle -> view?.showLoading(false)
                }
            }
        }
    }

    private fun handleFinished(result: TransactionExecutionState.Finished) {
        val info = TransactionInfo(
            transactionId = result.signature,
            status = R.string.main_send_success,
            message = R.string.main_send_transaction_confirmed,
            iconRes = R.drawable.ic_success,
            // Show usd and token amount from confirmed transaction
            amount = destinationAmount.toBigDecimalOrZero().scaleMedium().toString(),
            usdAmount = sourceToken.usdRateOrZero.multiply(sourceAmount.toBigDecimalOrZero()).scaleMedium().toString(),
            tokenSymbol = requireDestinationToken().tokenSymbol
        )
        view?.showSwapSuccess(info)
        view?.showLoading(false)
    }

    private fun handleFailed(result: TransactionExecutionState.Failed) {
        view?.showLoading(false)
        view?.showErrorMessage(result.throwable)
    }

    private fun requireDestinationToken(): Token =
        destinationToken ?: throw IllegalStateException("Destination token is null")
}