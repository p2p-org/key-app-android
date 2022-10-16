package org.p2p.wallet.solend.ui.withdraw

import kotlinx.coroutines.launch
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.interactor.SolendDepositsInteractor
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.getErrorMessage
import org.p2p.wallet.utils.isMoreThan
import timber.log.Timber
import java.math.BigDecimal

class SolendWithdrawPresenter(
    private val resourcesProvider: ResourcesProvider,
    private val solendDepositsInteractor: SolendDepositsInteractor,
    token: SolendDepositToken.Active
) : BasePresenter<SolendWithdrawContract.View>(), SolendWithdrawContract.Presenter {

    private var selectedDepositToken = token
    private var validDeposits: List<SolendDepositToken.Active> = emptyList()

    override fun attach(view: SolendWithdrawContract.View) {
        super.attach(view)
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
}
