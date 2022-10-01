package org.p2p.wallet.solend.ui.earn

import android.content.Context
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.getErrorMessage
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.launch

class SolendEarnPresenter(
    private val context: Context
) : BasePresenter<SolendEarnContract.View>(), SolendEarnContract.Presenter {

    override fun load() {
        view?.showLoading(isLoading = true)
        launch {
            try {
                // TODO: do request and show real data
                val tokens = listOf(
                    SolendDepositToken.Active(
                        "Solana",
                        "SOL",
                        "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/" +
                            "So11111111111111111111111111111111111111112/logo.png",
                        3.05f, BigDecimal.valueOf(100)
                    ),
                    SolendDepositToken.Inactive(
                        "Tether USD",
                        "USDT",
                        "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/" +
                            "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB/logo.svg",
                        2.05f
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
