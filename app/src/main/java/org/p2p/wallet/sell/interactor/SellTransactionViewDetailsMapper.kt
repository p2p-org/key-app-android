package org.p2p.wallet.sell.interactor

import android.content.res.Resources
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.formatUsd
import org.p2p.wallet.R
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails

class SellTransactionViewDetailsMapper(private val resources: Resources) {
    fun fromDomain(transactions: List<SellTransaction>): List<SellTransactionViewDetails> {
        return transactions.map {
            val receiverAddress = if (it is SellTransaction.WaitingForDepositTransaction) {
                it.moonpayDepositWalletAddress.base58Value
            } else {
                resources.getString(R.string.you_bank_account_via_moonpay)
            }
            SellTransactionViewDetails(
                status = it.status,
                formattedSolAmount = it.amounts.tokenAmount.formatToken(),
                formattedUsdAmount = it.getFiatAmount().formatUsd(),
                receiverAddress = receiverAddress
            )
        }
    }
}
