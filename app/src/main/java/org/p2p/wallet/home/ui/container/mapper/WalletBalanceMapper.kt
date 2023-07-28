package org.p2p.wallet.home.ui.container.mapper

import java.math.BigDecimal
import java.math.RoundingMode
import org.p2p.core.utils.formatFiat

private const val MAX_VALUE_STRING = "$999M+"

private val thousand = BigDecimal(1_000)
private val million = BigDecimal(1_000_000)
private val billion = BigDecimal(1_000_000_000)

class WalletBalanceMapper() {

    fun formatBalance(balance: BigDecimal): String {
        return when {
            balance < thousand -> {
                val formattedBalance = balance.setScale(0, RoundingMode.FLOOR)
                    .stripTrailingZeros()
                    .toPlainString()
                "$$formattedBalance"
            }
            balance < million -> {
                val valueInThousands = balance / thousand
                "$${valueInThousands.formatFiat()}K"
            }
            balance < billion -> {
                val valueInMillions = balance / million
                "$${valueInMillions.formatFiat()}M"
            }
            else -> MAX_VALUE_STRING
        }
    }
}
