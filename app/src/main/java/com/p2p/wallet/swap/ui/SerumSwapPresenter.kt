package com.p2p.wallet.swap.ui

import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.swap.interactor.SerumSwapInteractor
import com.p2p.wallet.swap.interactor.SwapInteractor
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.fromLamports
import com.p2p.wallet.utils.isMoreThan
import com.p2p.wallet.utils.isZero
import com.p2p.wallet.utils.scaleLong
import com.p2p.wallet.utils.scaleMedium
import com.p2p.wallet.utils.toBigDecimalOrZero
import com.p2p.wallet.utils.toLamports
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.solanaj.model.types.TokenAccountBalance
import timber.log.Timber
import java.math.BigDecimal
import kotlin.properties.Delegates

class SerumSwapPresenter(
    private val initialToken: Token?,
    private val userInteractor: UserInteractor,
    private val swapInteractor: SwapInteractor,
    private val serumSwapInteractor: SerumSwapInteractor
) : BasePresenter<SwapContract.View>(), SwapContract.Presenter {

    private var sourceToken: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    private var destinationToken: Token? by Delegates.observable(null) { _, _, newValue ->
        view?.showDestinationToken(newValue)
    }

    private var sourceBalance: TokenAccountBalance? = null
    private var destinationBalance: TokenAccountBalance? = null

    private var isReverse: Boolean = false
    private var reverseJob: Job? = null

    private var sourceAmount: String = "0"
    private var destinationAmount: String = "0"

    private var aroundValue: BigDecimal = BigDecimal.ZERO
    private var slippage: Double = 0.1

    private var calculationJob: Job? = null

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            val source = initialToken ?: userInteractor.getUserTokens().firstOrNull { it.isSRM } ?: return@launch
            sourceToken = source
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
        destinationToken = null
        view?.hidePrice()
        destinationAmount = "0"

        view?.hideCalculations()
        view?.showButtonText(R.string.main_select_token)

        updateButtonText(newToken)
        setButtonEnabled()
    }

    override fun setNewDestinationToken(newToken: Token) {
        destinationToken = newToken
        updateButtonText(sourceToken!!)
        setButtonEnabled()
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

    override fun loadPrice(toggle: Boolean) {
        if (toggle) isReverse = !isReverse

        reverseJob?.cancel()
        reverseJob = launch {
            try {
                val source = requireSourceToken()
                val destination = requireDestinationToken()
                val sourceSymbol = if (isReverse) destination.tokenSymbol else source.tokenSymbol
                val destinationSymbol = if (isReverse) source.tokenSymbol else destination.tokenSymbol
                val priceInSingleValue = userInteractor.getPriceByToken(sourceSymbol, destinationSymbol)
                view?.showPrice(priceInSingleValue, destinationSymbol, sourceSymbol)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading price")
            }
        }
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

        calculateData(requireSourceToken(), destinationToken)

        setButtonEnabled()
    }

    override fun reverseTokens() {
        val source = sourceToken
        sourceToken = destinationToken
        destinationToken = source

        setButtonEnabled()
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

    private fun calculateData(source: Token, destination: Token?) {
        if (destination == null) return
        calculationJob?.cancel()
        calculationJob = launch {

            val calculatedAmount = serumSwapInteractor.loadFair(
                fromMint = source.mintAddress,
                toMint = destination.mintAddress
            )

            destinationAmount = calculatedAmount.toString()

            val fee = serumSwapInteractor.calculateNetworkFee(
                fromWallet = source,
                toWallet = destination,
                lamportsPerSignature = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals)
            )

            val balanceA = swapInteractor.loadTokenBalance(source.publicKey.toPublicKey())
                .also { sourceBalance = it }

            val balanceB = swapInteractor.loadTokenBalance(destination.publicKey.toPublicKey())
                .also { destinationBalance = it }

            val minReceive = swapInteractor.calculateMinReceive(
                balanceA = balanceA,
                balanceB = balanceB,
                amount = sourceAmount.toBigDecimalOrZero().toLamports(source.decimals),
                slippage = slippage
            )

            val data = CalculationsData(
                destinationAmount = destinationAmount,
                minReceive = minReceive.fromLamports(destination.decimals).scaleMedium(),
                minReceiveSymbol = destination.tokenSymbol,
                fee = "${fee.fromLamports(destination.decimals).scaleMedium()} ${destination.tokenSymbol}",
                liquidityProviderFee = "0.0000075 BTC",
                feeSymbol = destination.tokenSymbol
            )

            updateButtonText(source)
            view?.showCalculations(data)
            view?.showSlippage(slippage)
        }
    }

    private fun setButtonEnabled() {
        val isMoreThanBalance = sourceAmount.toBigDecimalOrZero() > sourceToken?.total ?: BigDecimal.ZERO
        val isEnabled = sourceAmount.toBigDecimalOrZero().compareTo(BigDecimal.ZERO) != 0 &&
            !isMoreThanBalance && destinationToken != null
        view?.showButtonEnabled(isEnabled)
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

    private fun requireSourceToken(): Token =
        sourceToken ?: throw IllegalStateException("Source token is null")

    private fun requireDestinationToken(): Token =
        destinationToken ?: throw IllegalStateException("Destination token is null")
}

data class CalculationsData(
    val destinationAmount: String,
    val minReceive: BigDecimal,
    val minReceiveSymbol: String,
    val fee: String,
    val liquidityProviderFee: String,
    val feeSymbol: String
)