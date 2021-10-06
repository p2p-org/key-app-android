package com.p2p.wallet.swap.serum.ui

import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.swap.serum.interactor.SerumSwapInteractor
import com.p2p.wallet.swap.serum.interactor.SerumSwapAmountInteractor
import com.p2p.wallet.swap.serum.model.SerumAmountData
import com.p2p.wallet.swap.serum.model.SerumFeeType
import com.p2p.wallet.swap.model.PriceData
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.swap.serum.model.SerumSwapFee
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.fromLamports
import com.p2p.wallet.utils.isLessThan
import com.p2p.wallet.utils.isMoreThan
import com.p2p.wallet.utils.isNotZero
import com.p2p.wallet.utils.isZero
import com.p2p.wallet.utils.scaleLong
import com.p2p.wallet.utils.scaleMedium
import com.p2p.wallet.utils.toBigDecimalOrZero
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.Delegates

// TODO: Refactor this class, too complicated logic, it can be optimized
class SwapPresenter(
    private val initialToken: Token?,
    private val userInteractor: UserInteractor,
    private val swapInteractor: SerumSwapAmountInteractor,
    private val serumSwapInteractor: SerumSwapInteractor
) : BasePresenter<SerumSwapContract.View>(), SerumSwapContract.Presenter {

    private var sourceToken: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    private var destinationToken: Token? by Delegates.observable(null) { _, _, newValue ->
        view?.showDestinationToken(newValue)
    }

    private var sourceAmount: String = "0"
    private var destinationAmount: String = "0"

    private var aroundValue: BigDecimal = BigDecimal.ZERO
    private var slippage: Double = 0.5

    private var sourceRate: BigDecimal = BigDecimal.ZERO
    private var destinationRate: BigDecimal = BigDecimal.ZERO

    private var lamportsPerSignature: BigInteger = BigInteger.ZERO
    private var creatingAccountFee: BigInteger = BigInteger.ZERO
    private var liquidityProviderFee: BigDecimal = BigDecimal.ZERO

    private var minOrderSize: BigDecimal = BigDecimal.ZERO

    private var currentFees = mutableMapOf<SerumFeeType, SerumSwapFee>()

    private var calculationJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            sourceToken = initialToken ?: userInteractor.getUserTokens().find { it.isSOL }

            calculateAvailableAmount(sourceToken!!)

            lamportsPerSignature = swapInteractor.getLamportsPerSignature()
            liquidityProviderFee = swapInteractor.calculateLiquidityProviderFee()
            creatingAccountFee = swapInteractor.getCreatingTokenAccountFee()

            view?.showSlippage(slippage)
            view?.showFullScreenLoading(false)
        }
    }

    override fun loadTokensForSourceSelection() {
        launch {
            val tokens = userInteractor.getUserTokens()
            view?.openSourceSelection(tokens)
        }
    }

    override fun loadTokensForDestinationSelection() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val result = tokens.filter { token ->
                token.mintAddress != sourceToken?.mintAddress
            }
            view?.openDestinationSelection(result)
        }
    }

    override fun setNewSourceToken(newToken: Token) {
        if (sourceToken == newToken) return

        sourceToken = newToken

        calculateAvailableAmount(newToken)

        destinationToken = null
        view?.hidePrice()
        destinationAmount = "0"

        minOrderSize = BigDecimal.ZERO

        view?.hideCalculations()
        view?.showButtonText(R.string.main_select_token)

        updateButtonText(newToken)
        setButtonEnabled()
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken

        val source = sourceToken!!
        launch {
            calculateMinOrderSize(source.mintAddress, newToken.mintAddress)
            calculateRateAndFees(source, newToken)
            calculateInputAmount(source, newToken)
        }
    }

    override fun setSlippage(slippage: Double) {
        this.slippage = slippage
        view?.showSlippage(slippage)
    }

    override fun loadDataForSwapSettings() {
        view?.openSwapSettings(Slippage.parse(slippage))
    }

    override fun loadSlippage() {
        view?.openSlippageDialog(Slippage.parse(slippage))
    }

    override fun feedAvailableValue() {
        view?.updateInputValue(requireSourceToken().total.scaleLong())
    }

    override fun setSourceAmount(amount: String) {
        sourceAmount = amount
        val token = sourceToken ?: return

        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = token.usdRate.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(token.total)
        val availableColor = if (isMoreThanBalance) R.attr.colorAccentWarning else R.attr.colorAccentPrimary

        view?.setAvailableTextColor(availableColor)
        view?.showAroundValue(aroundValue)

        calculateInputAmount(requireSourceToken(), destinationToken)

        setButtonEnabled()
    }

    override fun reverseTokens() {
        if (sourceToken == null || destinationToken == null) return

        /* reversing tokens */
        val newSource = destinationToken!!
        val newDestination = sourceToken!!
        sourceToken = null

        /* reversing amounts */
        sourceAmount = destinationAmount
        destinationAmount = ""

        /* rate is being used at [calculateAmount], so reversing fields as well */
        val oldSourceRate = sourceRate
        sourceRate = destinationRate
        destinationRate = oldSourceRate

        setNewSourceToken(newSource)
        setNewDestinationToken(newDestination)
        view?.showButtonEnabled(false)
    }

    override fun swap() {
        launch {
            try {
                view?.showLoading(true)
                val finalAmount = sourceAmount.toBigDecimalOrZero()

                val transactionId = serumSwapInteractor.swap(
                    fromWallet = requireSourceToken(),
                    toWallet = requireDestinationToken(),
                    amount = finalAmount,
                    slippage = slippage
                )

                val info = TransactionInfo(
                    transactionId = transactionId,
                    status = R.string.main_send_success,
                    message = R.string.main_send_transaction_confirmed,
                    iconRes = R.drawable.ic_success,
                    amount = destinationAmount.toBigDecimalOrZero(),
                    usdAmount = aroundValue,
                    tokenSymbol = requireDestinationToken().tokenSymbol
                )
                view?.showSwapSuccess(info)
            } catch (e: Throwable) {
                Timber.e(e, "Error swapping tokens")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun calculateAvailableAmount(newToken: Token) {
        val availableAmount = swapInteractor.calculateAvailableAmount(newToken, currentFees[SerumFeeType.DEFAULT])
        val scaledAmount = availableAmount?.scaleLong() ?: BigDecimal.ZERO
        val available = "$scaledAmount ${newToken.tokenSymbol}"
        view?.showSourceAvailable(available)
    }

    private suspend fun calculateRateAndFees(source: Token, destination: Token) {
        try {
            view?.showButtonText(R.string.swap_calculating_fees)

            /* Load reverse price and caching it */
            sourceRate = swapInteractor.loadPrice(source.mintAddress, destination.mintAddress).scaleMedium()
            destinationRate = swapInteractor.loadPrice(destination.mintAddress, source.mintAddress).scaleMedium()

            val priceData = PriceData(
                sourceAmount = sourceRate.toString(),
                destinationAmount = destinationRate.toString(),
                sourceSymbol = source.tokenSymbol,
                destinationSymbol = destination.tokenSymbol
            )
            view?.showPrice(priceData)

            val fees = swapInteractor.calculateFees(
                sourceToken = source,
                destinationToken = destination,
                lamportsPerSignature = lamportsPerSignature,
                creatingAccountFee = creatingAccountFee
            )

            currentFees.putAll(fees)

            val liquidityFee = currentFees[SerumFeeType.LIQUIDITY_PROVIDER]?.stringValue.orEmpty()
            val defaultFee = currentFees[SerumFeeType.DEFAULT]

            val networkFee = if (defaultFee != null) {
                val formattedFee = defaultFee.lamports
                    .fromLamports(source.decimals)
                    .toFee()
                    .scaleMedium()

                "$formattedFee ${defaultFee.tokenSymbol}"
            } else null

            // FIXME: pay network option implementation
            val feeOption = Token.SOL_SYMBOL
            view?.showFees(networkFee = networkFee.orEmpty(), liquidityFee = liquidityFee, feeOption)
        } catch (e: Throwable) {
            Timber.e(e, "Error calculating network fees")
        } finally {
            updateButtonText(source)
        }
    }

    private fun calculateInputAmount(source: Token, destination: Token?) {
        if (destination == null) return
        calculationJob?.cancel()
        calculationJob = launch {
            val estimatedAmount = swapInteractor.calculateEstimatedAmount(
                inputAmount = sourceAmount.toDoubleOrNull(),
                rate = sourceRate.toDouble(),
                slippage = slippage
            )?.scaleMedium()

            destinationAmount = (estimatedAmount ?: BigDecimal.ZERO).toString()

            val data = SerumAmountData(
                destinationAmount = destinationAmount,
                estimatedReceiveAmount = estimatedAmount ?: BigDecimal.ZERO
            )

            updateButtonText(source)
            view?.showCalculations(data)
            view?.showSlippage(slippage)
        }

        setButtonEnabled()
    }

    private suspend fun calculateMinOrderSize(fromMint: String, toMint: String) {
        val minOrderSize = serumSwapInteractor.loadMinOrderSize(fromMint, toMint)
        this.minOrderSize = minOrderSize
    }

    private fun setButtonEnabled() {
        val sourceAmount = sourceAmount.toBigDecimalOrZero()
        val minOrderSizeFailed = sourceAmount.isLessThan(minOrderSize) && minOrderSize.isNotZero()
        val total = sourceToken?.total ?: BigDecimal.ZERO
        val isMoreThanBalance = sourceAmount.isMoreThan(total)
        val destinationValid = destinationToken != null
        val isEnabled = sourceAmount.isNotZero() && !isMoreThanBalance && destinationValid && !minOrderSizeFailed
        view?.showButtonEnabled(isEnabled)
    }

    private fun updateButtonText(source: Token) {
        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = source.usdRate.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(source.total)
        val minOrderSizeFailed = decimalAmount.isLessThan(minOrderSize)

        when {
            isMoreThanBalance -> view?.showButtonText(R.string.swap_funds_not_enough)
            decimalAmount.isZero() -> view?.showButtonText(R.string.main_enter_the_amount)
            destinationToken == null -> view?.showButtonText(R.string.main_select_token)
            minOrderSizeFailed -> view?.showButtonText(R.string.swap_min_order_size_amount, minOrderSize.toString())
            else -> view?.showButtonText(R.string.main_swap_now)
        }
    }

    private fun requireSourceToken(): Token =
        sourceToken ?: throw IllegalStateException("Source token is null")

    private fun requireDestinationToken(): Token =
        destinationToken ?: throw IllegalStateException("Destination token is null")
}

private fun BigDecimal.toFee() = this / BigDecimal(1000)