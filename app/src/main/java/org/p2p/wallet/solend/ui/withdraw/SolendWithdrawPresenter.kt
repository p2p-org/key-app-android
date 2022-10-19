package org.p2p.wallet.solend.ui.withdraw

import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.interactor.SolendDepositInteractor
import org.p2p.wallet.solend.interactor.SolendWithdrawInteractor
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.formatToken
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.isMoreThan
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.orZero
import org.p2p.wallet.utils.toLamports
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.launch

class SolendWithdrawPresenter(
    token: SolendDepositToken.Active,
    private val resourcesProvider: ResourcesProvider,
    private val solendDepositsInteractor: SolendDepositInteractor,
    private val solendWithdrawInteractor: SolendWithdrawInteractor
) : BasePresenter<SolendWithdrawContract.View>(), SolendWithdrawContract.Presenter {

    private var selectedDepositToken = token
    private var validDeposits: List<SolendDepositToken.Active> = emptyList()

    private var currentInput: BigDecimal = BigDecimal.ZERO
    private var currentOutput: BigDecimal = BigDecimal.ZERO

    override fun attach(view: SolendWithdrawContract.View) {
        super.attach(view)
        initializeTokens(view)
    }

    private fun initializeTokens(view: SolendWithdrawContract.View) {
        view.showTokenToWithdraw(
            depositToken = selectedDepositToken,
            withChevron = validDeposits.size > 1
        )
        if (validDeposits.isEmpty()) {
            launch {
                try {
                    validDeposits = solendDepositsInteractor.getUserDeposits()
                        .filterIsInstance<SolendDepositToken.Active>()
                        .filter { it.depositAmount.isMoreThan(BigDecimal.ZERO) }
                    validDeposits.find { it.tokenSymbol == selectedDepositToken.tokenSymbol }?.let {
                        selectTokenToWithdraw(it)
                    }
                } catch (e: Throwable) {
                    Timber.e(e, "Error fetching available withdraw tokens")
                    view.showUiKitSnackBar(e.getErrorMessage { res -> resourcesProvider.getString(res) })
                }
            }
        }
    }

    override fun selectTokenToWithdraw(tokenToWithdraw: SolendDepositToken.Active) {
        if (selectedDepositToken != tokenToWithdraw) {
            selectedDepositToken = tokenToWithdraw
        }
        view?.showTokenToWithdraw(
            depositToken = tokenToWithdraw,
            withChevron = validDeposits.size > 1
        )
    }

    override fun onTokenWithdrawClicked() {
        view?.showTokensToWithdraw(validDeposits)
    }

    override fun updateInputs(input: BigDecimal, output: BigDecimal) {
        this.currentInput = input
        this.currentOutput = output

        val maxDepositAmount = selectedDepositToken.depositAmount.orZero()
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

    override fun withdraw() {
        launch {
            try {
                val amountInLamports = currentInput.toLamports(selectedDepositToken.decimals)
                solendWithdrawInteractor.withdraw(selectedDepositToken, amountInLamports)
                view?.showIndefiniteInfoMessage(
                    messageRes = R.string.solend_withdraw_in_progress,
                    actionButtonRes = R.string.common_hide
                )
                view?.navigateToEarnScreen()
            } catch (e: Throwable) {
                Timber.e(e, "Error withdrawing from ${selectedDepositToken.tokenSymbol}")
                view?.showErrorMessage(e)
            }
        }
    }
}
