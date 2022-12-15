package org.p2p.wallet.history.ui.token.adapter

import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal

sealed class MoonpayTransactionItem {
    abstract val amountInSol: BigDecimal
    abstract val amountInUsd: BigDecimal

    data class WaitingForDepositItem(
        val moonpayWalletAddress: Base58String,
        override val amountInSol: BigDecimal,
        override val amountInUsd: BigDecimal
    ) : MoonpayTransactionItem()

    data class TransactionPendingItem(
        override val amountInSol: BigDecimal,
        override val amountInUsd: BigDecimal
    ) : MoonpayTransactionItem()

    data class TransactionCompletedItem(
        override val amountInSol: BigDecimal,
        override val amountInUsd: BigDecimal
    ) : MoonpayTransactionItem()

    data class TransactionFailedItem(
        override val amountInSol: BigDecimal,
        override val amountInUsd: BigDecimal
    ) : MoonpayTransactionItem()
}
