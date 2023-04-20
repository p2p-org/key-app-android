package org.p2p.wallet.solend.ui.deposit

import org.p2p.wallet.R
import android.content.res.Resources
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.interactor.SolendDepositInteractor
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendTransactionDetails
import org.p2p.wallet.solend.model.SolendTransactionDetailsState
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.core.utils.formatToken
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.core.utils.toLamports
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FEE_DELAY_IN_MS = 250L

class SolendDepositPresenter(
    deposit: SolendDepositToken,
    private val resources: Resources,
    private val solendDepositInteractor: SolendDepositInteractor,
    private val depositInteractor: SolendDepositInteractor,
    private val orcaInfoInteractor: OrcaInfoInteractor
) : BasePresenter<SolendDepositContract.View>(), SolendDepositContract.Presenter {

    private var selectedDepositToken = deposit
    private var validDeposits: List<SolendDepositToken> = emptyList()

    private var currentInput: BigDecimal = BigDecimal.ZERO
    private var currentOutput: BigDecimal = BigDecimal.ZERO

    private var calculateFeeJob: Job? = null

    override fun attach(view: SolendDepositContract.View) {
        super.attach(view)
        loadOrcaInfo()

        view.showTokenToDeposit(
            depositToken = selectedDepositToken,
            withChevron = validDeposits.size > 1
        )
    }

    override fun initialize(userDeposits: List<SolendDepositToken>) {
        if (validDeposits.isEmpty()) {
            view?.showFullScreenLoading(isLoading = true)
            launch {
                try {
                    validDeposits = userDeposits.filter { deposit -> deposit.availableTokensForDeposit.isNotZero() }

                    val validDeposit = validDeposits.find { it.tokenSymbol == selectedDepositToken.tokenSymbol }
                    if (validDeposit != null) selectTokenToDeposit(validDeposit)
                } catch (e: Throwable) {
                    Timber.e(e, "Error fetching available deposit tokens")
                    view?.showUiKitSnackBar(e.getErrorMessage { res -> resources.getString(res) })
                } finally {
                    view?.showFullScreenLoading(isLoading = false)
                }
            }
        }
    }

    override fun selectTokenToDeposit(tokenToDeposit: SolendDepositToken) {
        if (selectedDepositToken != tokenToDeposit) {
            selectedDepositToken = tokenToDeposit
        }
        view?.showTokenToDeposit(
            depositToken = tokenToDeposit,
            withChevron = validDeposits.size > 1
        )
    }

    override fun onTokenDepositClicked() {
        view?.showTokensToDeposit(validDeposits)
    }

    override fun updateInputs(input: BigDecimal, output: BigDecimal) {
        this.currentInput = input
        this.currentOutput = output

        calculateFee(input, output)
    }

    override fun deposit() {
        launch {
            try {
                val amountInLamports = currentInput.toLamports(selectedDepositToken.decimals)
                solendDepositInteractor.deposit(selectedDepositToken, amountInLamports)
            } catch (e: Throwable) {
                Timber.e(e, "Error while depositing to ${selectedDepositToken.tokenSymbol}")
                view?.showErrorMessage(e)
            }
        }
    }

    private fun calculateFee(input: BigDecimal, output: BigDecimal) {
        if (input.isZero()) {
            calculateFeeJob?.cancel()
            view?.setEmptyAmountState()
            return
        }

        calculateFeeJob?.cancel()
        calculateFeeJob = launch {
            delay(FEE_DELAY_IN_MS)

            view?.showFeeLoading(isLoading = true)
            val amountInLamports = currentInput.toLamports(selectedDepositToken.decimals)
            val amountInUsd = (currentInput * selectedDepositToken.usdRate).scaleShort()

            val fee = try {
                depositInteractor.calculateDepositFee(
                    amountInLamports = amountInLamports,
                    token = selectedDepositToken
                )
            } catch (e: CancellationException) {
                Timber.w(e, "Calculate deposit fees was cancelled")
                return@launch
            } catch (e: Throwable) {
                Timber.e(e, "Error calculating deposit fees")
                view?.showUiKitSnackBar(messageResId = R.string.error_calculating_fees)
                return@launch
            }

            val total = fee.getTotalFee(
                currentInput = currentInput,
                selectedDepositToken = selectedDepositToken,
                amountInLamports = amountInLamports
            )

            val detailsData = SolendTransactionDetails(
                amount = "$input ${fee.tokenSymbol} (~$ $amountInUsd)",
                transferFee = fee.getTransferFee(),
                fee = fee.getRentFee(),
                total = total
            )
            updateDepositState(input, output, detailsData)

            view?.showFeeLoading(isLoading = false)
        }
    }

    private fun updateDepositState(input: BigDecimal, output: BigDecimal, data: SolendTransactionDetails) {
        val maxDepositAmount = selectedDepositToken.availableTokensForDeposit.orZero()
        val tokenAmount = buildString {
            append(maxDepositAmount.formatToken())
            append(" ")
            append(selectedDepositToken.tokenSymbol)
        }
        val isBiggerThenMax = input > maxDepositAmount
        when {
            input.isZero() && output.isZero() -> view?.setEmptyAmountState()
            isBiggerThenMax -> view?.setBiggerThenMaxAmountState(tokenAmount)
            else -> view?.setValidDepositState(output, tokenAmount, SolendTransactionDetailsState.Deposit(data))
        }
    }

    private fun loadOrcaInfo() {
        view?.showFullScreenLoading(isLoading = true)
        launch {
            try {
                orcaInfoInteractor.load()
            } catch (e: Throwable) {
                Timber.e(e, "Error initializing orca")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            } finally {
                view?.showFullScreenLoading(isLoading = false)
            }
        }
    }
}
