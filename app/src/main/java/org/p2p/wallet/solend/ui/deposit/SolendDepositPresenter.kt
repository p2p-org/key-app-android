package org.p2p.wallet.solend.ui.deposit

import kotlinx.coroutines.launch
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.interactor.SolendDepositsInteractor
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.getErrorMessage
import timber.log.Timber

class SolendDepositPresenter(
    private val resourcesProvider: ResourcesProvider,
    private val solendDepositsInteractor: SolendDepositsInteractor,
    private val userInteractor: UserInteractor,
    deposit: SolendDepositToken
) : BasePresenter<SolendDepositContract.View>(), SolendDepositContract.Presenter {

    private var selectedDepositToken = deposit
    private var validDeposits: List<SolendDepositToken> = emptyList()

    override fun attach(view: SolendDepositContract.View) {
        super.attach(view)
        view.showTokenToDeposit(
            depositToken = selectedDepositToken,
            withChevron = validDeposits.size > 1
        )
        if (validDeposits.isEmpty()) {
            launch {
                try {
                    val userTokens = userInteractor.getUserTokens()
                    validDeposits = solendDepositsInteractor.getUserDeposits().onEach { depositToken ->
                        depositToken.availableTokensForDeposit = userTokens.firstOrNull { token ->
                            token.tokenSymbol == depositToken.tokenSymbol
                        }?.total
                    }.filter { it.availableTokensForDeposit != null }
                    validDeposits.find { it.tokenSymbol == selectedDepositToken.tokenSymbol }?.let {
                        selectTokenToDeposit(it)
                    }
                } catch (e: Throwable) {
                    Timber.e(e, "Error fetching available deposit tokens")
                    view.showErrorSnackBar(e.getErrorMessage(resourcesProvider))
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
}
