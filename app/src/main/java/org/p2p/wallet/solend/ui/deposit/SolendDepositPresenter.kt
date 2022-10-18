package org.p2p.wallet.solend.ui.deposit

import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.interactor.SolendDepositInteractor
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.formatToken
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.orZero
import org.p2p.wallet.utils.toLamports
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.launch

class SolendDepositPresenter(
    private val resourcesProvider: ResourcesProvider,
    private val solendDepositInteractor: SolendDepositInteractor,
    deposit: SolendDepositToken
) : BasePresenter<SolendDepositContract.View>(), SolendDepositContract.Presenter {

    private var selectedDepositToken = deposit
    private var validDeposits: List<SolendDepositToken> = emptyList()

    private var currentInput: BigDecimal = BigDecimal.ZERO
    private var currentOutput: BigDecimal = BigDecimal.ZERO

    override fun attach(view: SolendDepositContract.View) {
        super.attach(view)
        view.showTokenToDeposit(
            depositToken = selectedDepositToken,
            withChevron = validDeposits.size > 1
        )
        if (validDeposits.isEmpty()) {
            launch {
                try {
                    validDeposits = solendDepositInteractor.getUserDeposits().filter { deposit ->
                        deposit.availableTokensForDeposit.isNotZero()
                    }

                    val validDeposit = validDeposits.find { it.tokenSymbol == selectedDepositToken.tokenSymbol }
                    if (validDeposit != null) selectTokenToDeposit(validDeposit)
                } catch (e: Throwable) {
                    Timber.e(e, "Error fetching available deposit tokens")
                    view.showUiKitSnackBar(e.getErrorMessage { res -> resourcesProvider.getString(res) })
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
            else -> view?.setValidDepositState(output, tokenAmount)
        }
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
}
