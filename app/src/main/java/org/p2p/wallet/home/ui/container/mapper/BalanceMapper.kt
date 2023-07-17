package org.p2p.wallet.home.ui.container.mapper

import java.math.BigDecimal
import org.p2p.core.utils.asUsd
import org.p2p.core.utils.formatFiat

private const val MAX_VALUE_STRING = "$999M+"

private val thousand = BigDecimal(1_000)
private val million = BigDecimal(1_000_000)
private val billion = BigDecimal(1_000_000_000)

class BalanceMapper() {

    fun mapBalanceForWallet(balance: BigDecimal): String {
        return when {
            balance < thousand -> balance.asUsd()
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
