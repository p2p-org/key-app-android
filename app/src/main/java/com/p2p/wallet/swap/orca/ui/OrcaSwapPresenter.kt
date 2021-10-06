package com.p2p.wallet.swap.orca.ui

import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.swap.model.PriceData
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.swap.orca.interactor.OrcaSwapInteractor
import com.p2p.wallet.swap.orca.model.OrcaAmountData
import com.p2p.wallet.swap.orca.model.OrcaFeeData
import com.p2p.wallet.swap.orca.model.OrcaSwapRequest
import com.p2p.wallet.swap.orca.model.OrcaSwapResult
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
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.TokenAccountBalance
import timber.log.Timber
import java.math.BigDecimal
import kotlin.properties.Delegates

class OrcaSwapPresenter(
    private val initialToken: Token?,
    private val userInteractor: UserInteractor,
    private val swapInteractor: OrcaSwapInteractor
) : BasePresenter<OrcaSwapContract.View>(), OrcaSwapContract.Presenter {

    private lateinit var sourceToken: Token

    private var destinationToken: Token? by Delegates.observable(null) { _, _, newValue ->
        view?.showDestinationToken(newValue)
    }

    private var currentPool: Pool.PoolInfo? = null
    private lateinit var sourceBalance: TokenAccountBalance
    private lateinit var destinationBalance: TokenAccountBalance

    private var sourceRate: BigDecimal = BigDecimal.ZERO
    private var destinationRate: BigDecimal = BigDecimal.ZERO

    private var sourceAmount: String = "0"
    private var destinationAmount: String = "0"

    private var aroundValue: BigDecimal = BigDecimal.ZERO
    private var slippage: Double = 0.1

    init {
        launch {
            val token = initialToken ?: userInteractor.getUserTokens().first { it.isSOL }
            setSourceToken(token)
        }
    }

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            try {
                swapInteractor.loadAllPools()
                view?.showSlippage(slippage)
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
            val tokens = userInteractor.getUserTokens()
            val pools = swapInteractor.getAllPools()
            val result = tokens.filter { token -> filterDestinationTokens(pools, token) }
            view?.openDestinationSelection(result)
        }
    }

    override fun setNewSourceToken(newToken: Token) {
        setSourceToken(newToken)
        clearDestination()
        updateButtonText(newToken)
        setButtonEnabled()
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken
        calculateData(newToken)
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
        view?.updateInputValue(sourceToken.total.scaleLong())
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
        if (destinationToken == null) return

        val source = sourceToken
        setSourceToken(destinationToken!!)
        destinationToken = source

        calculateData(destinationToken!!)
    }

    override fun swap() {
        launch {
            try {
                view?.showLoading(true)
                val lamports = sourceAmount.toBigDecimalOrZero().toLamports(sourceToken.decimals)

                val request = OrcaSwapRequest(
                    pool = requirePool(),
                    slippage = slippage,
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
            calculateFees(sourceToken, destination, requirePool())
            calculateRates(sourceToken, destination)
            calculateAmount(sourceToken, destination, requirePool())
        }
    }

    private suspend fun calculateRates(source: Token, destination: Token) {
        sourceRate = userInteractor.getPriceByToken(source.tokenSymbol, destination.tokenSymbol)
        destinationRate = userInteractor.getPriceByToken(destination.tokenSymbol, source.tokenSymbol)

        val priceData = PriceData(
            sourceAmount = sourceRate.toString(),
            destinationAmount = destinationRate.toString(),
            sourceSymbol = source.tokenSymbol,
            destinationSymbol = destination.tokenSymbol
        )
        view?.showPrice(priceData)
    }

    private suspend fun calculatePoolAndBalance(source: Token, destination: Token) {
        val sourceMint = source.mintAddress
        val destinationMint = destination.mintAddress

        /**
         * Pool cannot be empty because we are filtering tokens by pool list pairs
         * */
        val pool = swapInteractor.findPool(sourceMint, destinationMint)!!
        currentPool = pool

        sourceBalance = swapInteractor.loadTokenBalance(pool.tokenAccountA)
        destinationBalance = swapInteractor.loadTokenBalance(pool.tokenAccountB)
    }

    private fun calculateFees(source: Token, destination: Token, pool: Pool.PoolInfo) {
        val enteredAmount = sourceAmount.toBigDecimalOrZero()
        if (enteredAmount.isZero()) {
            view?.hideCalculations()
            return
        }

        val fee = swapInteractor.calculateFee(
            pool = pool,
            inputAmount = enteredAmount.toLamports(source.decimals),
            tokenABalance = sourceBalance,
            tokenBBalance = destinationBalance
        )

        val networkFee = "${fee.fromLamports(destination.decimals).scaleMedium()} ${destination.tokenSymbol}"

        // FIXME: calculate fee
        val liquidityProviderFee = "N/A"

        // FIXME: pay network option implementation
        val feePaymentOption = Token.SOL_SYMBOL
        val data = OrcaFeeData(networkFee, liquidityProviderFee, feePaymentOption)
        view?.showFees(data)
    }

    private fun calculateAmount(source: Token, destination: Token, pool: Pool.PoolInfo) {
        val balanceA = sourceBalance
        val balanceB = destinationBalance
        val amountInOtherToken = swapInteractor.calculateAmountInOtherToken(
            pool = pool,
            inputAmount = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals),
            withFee = false,
            tokenABalance = balanceA,
            tokenBBalance = balanceB
        )
        val calculatedAmount = amountInOtherToken.fromLamports(destination.decimals).scaleMedium()

        destinationAmount = calculatedAmount.toString()

        val minReceive = swapInteractor.calculateMinReceive(
            balanceA = balanceA,
            balanceB = balanceB,
            amount = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals),
            slippage = slippage
        )

        val data = OrcaAmountData(
            destinationAmount = destinationAmount,
            estimatedReceiveAmount = minReceive.fromLamports(destination.decimals).scaleMedium(),
            estimatedReceiveSymbol = destination.tokenSymbol
        )

        view?.showCalculations(data)
        updateButtonText(source)
        setButtonEnabled()
    }

    private fun clearDestination() {
        destinationToken = null
        destinationAmount = "0"
        view?.hidePrice()

        view?.hideCalculations()
        view?.showButtonText(R.string.main_select_token)
    }

    private fun filterSourceTokens(token: Token, pools: List<Pool.PoolInfo>): Boolean {
        if (token.isZero) return false
        if (destinationToken == null) return true

        return pools.any {
            val containsMintA = it.swapData.mintA.toBase58() == token.mintAddress
            val containsMintB = it.swapData.mintB.toBase58() == token.mintAddress
            containsMintA || containsMintB
        }
    }

    private fun filterDestinationTokens(
        pools: List<Pool.PoolInfo>,
        token: Token
    ) = pools.any {
        it.swapData.mintB.toBase58() == token.mintAddress &&
            it.swapData.mintA.toBase58() == sourceToken.mintAddress ||
            it.swapData.mintA.toBase58() == token.mintAddress &&
            it.swapData.mintB.toBase58() == sourceToken.mintAddress
    } && token.publicKey != sourceToken.publicKey

    private fun setButtonEnabled() {
        val isMoreThanBalance = sourceAmount.toBigDecimalOrZero() > sourceToken.total
        val isValidAmount = sourceAmount.toBigDecimalOrZero().isNotZero()
        val isEnabled = isValidAmount && !isMoreThanBalance && destinationToken != null
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
            amount = result.receivedAmount,
            usdAmount = result.usdReceivedAmount,
            tokenSymbol = result.tokenSymbol
        )
        view?.showSwapSuccess(info)
    }

    private fun updateButtonText(source: Token) {
        val decimalAmount = sourceAmount.toBigDecimalOrZero()
        aroundValue = source.usdRate.multiply(decimalAmount).scaleMedium()

        val isMoreThanBalance = decimalAmount.isMoreThan(source.total)

        when {
            isMoreThanBalance -> view?.showButtonText(R.string.swap_funds_not_enough)
            decimalAmount.isZero() -> view?.showButtonText(R.string.main_enter_the_amount)
            destinationToken == null -> view?.showButtonText(R.string.main_select_token)
            else -> view?.showButtonText(R.string.main_swap_now)
        }
    }

    private fun setSourceToken(token: Token) {
        sourceToken = token
        view?.showSourceToken(sourceToken)

        /* Calculating available amount */
        val availableAmount = token.total.scaleLong()
        val available = "$availableAmount ${token.tokenSymbol}"
        view?.showSourceAvailable(available)
    }

    private fun requirePool(): Pool.PoolInfo =
        currentPool ?: throw IllegalStateException("Pool is null")

    private fun requireDestinationToken(): Token =
        destinationToken ?: throw IllegalStateException("Destination token is null")
}