package org.p2p.wallet.solend.ui.deposits

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.getErrorMessage
import timber.log.Timber
import java.math.BigDecimal

class SolendUserDepositsPresenter(
    private val context: Context
) : BasePresenter<SolendUserDepositsContract.View>(), SolendUserDepositsContract.Presenter {

    override fun attach(view: SolendUserDepositsContract.View) {
        super.attach(view)
        loadData()
    }

    override fun onAddMoreClicked(token: SolendDepositToken) {
        // TODO PWN-5020 make real impl
    }

    override fun onWithdrawClicked(token: SolendDepositToken) {
        // TODO PWN-5020 make real impl
    }

    private fun loadData() {
        view?.showLoading(isLoading = true)
        launch {
            try {
                // TODO: do request and show real data
                delay(1000L)
                val tokens = listOf(
                    SolendDepositToken.Active(
                        "Solana",
                        "SOL",
                        "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/" +
                            "So11111111111111111111111111111111111111112/logo.png",
                        BigDecimal("3.05"), BigDecimal.valueOf(100)
                    ),
                    SolendDepositToken.Inactive(
                        "Tether USD",
                        "USDT",
                        "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/" +
                            "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB/logo.svg",
                        BigDecimal("2.05")
                    )
                )
                view?.showTokens(tokens)
            } catch (e: Throwable) {
                Timber.e(e, "Error fetching available deposit tokens")
                view?.showSuccessSnackBar(e.getErrorMessage(context))
            } finally {
                view?.showLoading(isLoading = false)
            }
        }
    }
}
