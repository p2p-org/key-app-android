package com.p2p.wallet.swap.orca.ui

import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.swap.model.AccountBalance
import com.p2p.wallet.swap.model.PriceData
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.swap.orca.interactor.OrcaSwapAmountInteractor
import com.p2p.wallet.swap.orca.interactor.OrcaSwapInteractor
import com.p2p.wallet.swap.orca.model.OrcaAmountData
import com.p2p.wallet.swap.orca.model.OrcaFeeData
import com.p2p.wallet.swap.orca.model.OrcaPool
import com.p2p.wallet.swap.orca.model.OrcaSwapRequest
import com.p2p.wallet.swap.orca.model.OrcaSwapResult
import com.p2p.wallet.swap.orca.model.ValidOrcaPool
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.fromLamports
import com.p2p.wallet.utils.isMoreThan
import com.p2p.wallet.utils.isNotZero
import com.p2p.wallet.utils.isZero
import com.p2p.wallet.utils.scaleLong
import com.p2p.wallet.utils.scaleMedium
import com.p2p.wallet.utils.toBigDecimalOrZero
import com.p2p.wallet.utils.toLamports
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.Delegates

class OrcaSwapPresenter(
    private val initialToken: Token.Active?,
    private val userInteractor: UserInteractor,
    private val swapInteractor: OrcaSwapInteractor,
    private val amountInteractor: OrcaSwapAmountInteractor
) : BasePresenter<OrcaSwapContract.View>(), OrcaSwapContract.Presenter {

    private lateinit var sourceToken: Token.Active

    private var destinationToken: Token? by Delegates.observable(null) { _, _, newValue ->
        view?.showDestinationToken(newValue)
    }

    private var currentPool: ValidOrcaPool? = null
    private lateinit var sourceBalance: AccountBalance
    private lateinit var destinationBalance: AccountBalance

    private var sourceRate: BigDecimal = BigDecimal.ZERO
    private var destinationRate: BigDecimal = BigDecimal.ZERO

    private var lamportsPerSignature: BigInteger = BigInteger.ZERO
    private var minRentExemption: BigInteger = BigInteger.ZERO

    private var sourceAmount: String = "0"
    private var destinationAmount: String = "0"

    private var aroundValue: BigDecimal = BigDecimal.ZERO
    private var slippage: Slippage = Slippage.MEDIUM

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            try {
                val token = initialToken ?: userInteractor.getUserTokens().first { it.isSOL }
                setSourceToken(token)

                view?.showSlippage(slippage)

                swapInteractor.loadAllPools()

                lamportsPerSignature = swapInteractor.getLamportsPerSignature()
                minRentExemption = swapInteractor.getAccountMinForRentExemption()
            } catch (e: Throwable) {
                Timber.e(e, "Error loading all pools")
                view?.showErrorMessage(e)
            } finally {
                view?.showFullScreenLoading(false)
            }
        }
    }

    override fun loadTokensForSourceSelection() {
        launch {
            val tokens = userInteractor.getUserTokens()
            val pools = swapInteractor.getAllPools()
            val result = tokens.filter { token -> filterSourceTokens(token, pools) }
            view?.openSourceSelection(result)
        }
    }

    override fun loadTokensForDestinationSelection() {
        launch {
            val result = swapInteractor.getAvailableDestinationTokens(sourceToken)
            view?.openDestinationSelection(result)
        }
    }

    override fun setNewSourceToken(newToken: Token.Active) {
        setSourceToken(newToken)
        clearDestination()
        updateButtonText(newToken)
        setButtonEnabled()
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken
        calculateData(newToken)
    }

    override fun setSlippage(slippage: Slippage) {
        this.slippage = slippage
        view?.showSlippage(this.slippage)

        currentPool?.let {
            /* If pool is not null, then destination token is not null as well */
            calculateAmount(sourceToken, requireDestinationToken(), it)
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
        sourceAmount = amount

        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = sourceToken.usdRate.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(sourceToken.total)
        val availableColor = if (isMoreThanBalance) R.attr.colorAccentWarning else R.attr.colorAccentPrimary

        view?.setAvailableTextColor(availableColor)
        view?.showAroundValue(aroundValue)

        currentPool?.let {
            /* If pool is not null, then destination token is not null as well */
            calculateAmount(sourceToken, destinationToken!!, it)

            /* Fee is being calculated including entered amount, thus calculating fee if entered amount changed */
            calculateFees(sourceToken, destinationToken!!, it)
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

        /* reverse token balances */
        val sourceBalanceOld = sourceBalance
        destinationBalance = sourceBalance
        sourceBalance = sourceBalanceOld

        /* This trigger recalculation */
        view?.showNewAmount(sourceAmount)
    }

    override fun swap() {
        launch {
            try {
                view?.showLoading(true)
                val lamports = sourceAmount.toBigDecimalOrZero().toLamports(sourceToken.decimals)
                val request = OrcaSwapRequest(
                    pool = requirePool().orcaPool,
                    slippage = slippage.doubleValue,
                    amount = lamports,
                    balanceA = sourceBalance,
                    balanceB = destinationBalance
                )
                val data = swapInteractor.swap(
                    request = request,
                    receivedAmount = destinationAmount.toBigDecimalOrZero(),
                    usdReceivedAmount = aroundValue,
                    tokenSymbol = requireDestinationToken().tokenSymbol
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
            view?.showButtonText(R.string.swap_calculating_fees)
            calculatePoolAndBalance(sourceToken, destination)
            calculateRates(sourceToken, destination, requirePool())
            calculateAmount(sourceToken, destination, requirePool())
            calculateFees(sourceToken, destination, requirePool())
        }
    }

    private fun calculateRates(source: Token.Active, destination: Token, pool: ValidOrcaPool) {
        sourceRate = amountInteractor.calculateEstimatedAmount(
            inputAmount = BigDecimal.ONE.toLamports(source.decimals),
            includeFees = true,
            tokenABalance = sourceBalance,
            tokenBBalance = destinationBalance,
            pool = pool.orcaPool
        ).fromLamports(destination.decimals).scaleMedium()

        destinationRate = amountInteractor.calculateEstimatedAmount(
            inputAmount = BigDecimal.ONE.toLamports(destination.decimals),
            includeFees = true,
            tokenABalance = destinationBalance,
            tokenBBalance = sourceBalance,
            pool = pool.orcaPool
        ).fromLamports(source.decimals).scaleMedium()

        val priceData = PriceData(
            sourceAmount = sourceRate.toString(),
            destinationAmount = destinationRate.toString(),
            sourceSymbol = destination.tokenSymbol,
            destinationSymbol = source.tokenSymbol
        )
        view?.showPrice(priceData)
    }

    private suspend fun calculatePoolAndBalance(source: Token.Active, destination: Token) {
        val sourceMint = source.mintAddress
        val destinationMint = destination.mintAddress

        val pool = swapInteractor.findValidPool(sourceMint, destinationMint)

        if (pool == null) {
            view?.showError(R.string.error_unable_to_swap_this_pair)
            setButtonEnabled()
            return
        }

        view?.showError(null)
        currentPool = pool

        sourceBalance = pool.balanceA
        destinationBalance = pool.balanceB
    }

    private fun calculateFees(source: Token.Active, destination: Token, pool: ValidOrcaPool) {
        val enteredAmount = sourceAmount.toBigDecimalOrZero()
        if (enteredAmount.isZero()) {
            view?.hideCalculations()
            return
        }

        val liquidityFee = amountInteractor.calculateLiquidityFee(
            inputAmount = enteredAmount.toLamports(source.decimals),
            pool = pool.orcaPool,
            tokenABalance = sourceBalance,
            tokenBBalance = destinationBalance,
        ).fromLamports(destination.decimals).scaleMedium()

        val liquidityProviderFee = "$liquidityFee ${destination.tokenSymbol}"

        val networkFee = swapInteractor.calculateNetworkFee(
            source = source,
            destination = destination,
            lamportsPerSignature = lamportsPerSignature,
            minRentExemption = minRentExemption
        ).fromLamports().scaleMedium()

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

    private fun calculateAmount(source: Token.Active, destination: Token, pool: ValidOrcaPool) {
        val inputAmount = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals)

        val estimatedAmount = amountInteractor.calculateAmountInOtherToken(
            inputAmount = inputAmount,
            includeFees = true,
            tokenABalance = sourceBalance,
            tokenBBalance = destinationBalance,
            pool = pool.orcaPool
        ).fromLamports(destination.decimals).scaleMedium()

        destinationAmount = estimatedAmount.toString()

        val minReceive = amountInteractor.calculateMinReceive(
            inputAmount = inputAmount,
            slippage = slippage.doubleValue,
            includesFees = true,
            tokenABalance = sourceBalance,
            tokenBBalance = destinationBalance,
            pool = pool.orcaPool
        ).fromLamports(destination.decimals).scaleMedium()

        val data = OrcaAmountData(destinationAmount, minReceive)
        view?.showCalculations(data)
        updateButtonText(source)
        setButtonEnabled()
    }

    private fun setSourceToken(token: Token.Active) {
        destinationToken = null
        currentPool = null

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

    private fun filterSourceTokens(token: Token.Active, pools: List<OrcaPool>): Boolean {
        if (token.isZero) return false
        if (destinationToken == null) return true

        return pools.any {
            val containsMintA = it.sourceMint.toBase58() == token.mintAddress
            val containsMintB = it.destinationMint.toBase58() == token.mintAddress
            containsMintA || containsMintB
        }
    }

    private fun setButtonEnabled() {
        val amount = sourceAmount.toBigDecimalOrZero()
        val isMoreThanBalance = amount > sourceToken.total
        val isValidAmount = amount.isNotZero()
        val isPoolValid = currentPool != null
        val isEnabled = isValidAmount && !isMoreThanBalance && destinationToken != null && isPoolValid
        view?.showButtonEnabled(isEnabled)
    }

    private fun handleResult(result: OrcaSwapResult) {
        when (result) {
            is OrcaSwapResult.Success -> handleSuccess(result)
            is OrcaSwapResult.Error -> view?.showErrorMessage(result.messageRes)
        }
    }

    private fun handleSuccess(result: OrcaSwapResult.Success) {
        val info = TransactionInfo(
            transactionId = result.transactionId,
            status = R.string.main_send_success,
            message = R.string.main_send_transaction_confirmed,
            iconRes = R.drawable.ic_success,
            amount = "+${result.receivedAmount}",
            usdAmount = "+${result.usdReceivedAmount}",
            tokenSymbol = result.tokenSymbol
        )
        view?.showSwapSuccess(info)
    }

    private fun updateButtonText(source: Token.Active) {
        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = source.usdRate.multiply(decimalAmount).scaleMedium()

        val zeroRates = sourceRate.isZero() && destinationRate.isZero()

        val isMoreThanBalance = decimalAmount.isMoreThan(source.total)

        when {
            isMoreThanBalance -> view?.showButtonText(R.string.swap_funds_not_enough)
            decimalAmount.isZero() -> view?.showButtonText(R.string.main_enter_the_amount)
            destinationToken == null -> view?.showButtonText(R.string.main_select_token)
            zeroRates -> view?.showButtonText(R.string.swap_unable_to_swap_pair)
            else -> view?.showButtonText(R.string.main_swap_now)
        }
    }

    private fun requirePool(): ValidOrcaPool =
        currentPool ?: throw IllegalStateException("Pool is null")

    private fun requireDestinationToken(): Token =
        destinationToken ?: throw IllegalStateException("Destination token is null")
}