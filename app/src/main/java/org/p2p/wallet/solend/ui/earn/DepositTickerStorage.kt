package org.p2p.wallet.solend.ui.earn

import android.content.SharedPreferences
import androidx.core.content.edit
import java.math.BigDecimal

private const val KEY_DEPOSIT_TICKER_BALANCE = "KEY_DEPOSIT_TICKER_BALANCE"

class DepositTickerStorage(
    private val sharedPreferences: SharedPreferences
) {

    fun setLastTickerBalance(balance: BigDecimal) {
        sharedPreferences.edit {
            putString(KEY_DEPOSIT_TICKER_BALANCE, balance.toString())
        }
    }

    fun getTickerBalance(currentBalance: BigDecimal): BigDecimal = listOf(
        currentBalance,
        BigDecimal(
            sharedPreferences.getString(
                KEY_DEPOSIT_TICKER_BALANCE,
                currentBalance.toString()
            )
        )
    ).maxOrNull() ?: BigDecimal.ZERO
}
