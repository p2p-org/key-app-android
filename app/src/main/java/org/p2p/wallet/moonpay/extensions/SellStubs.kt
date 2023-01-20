package org.p2p.wallet.moonpay.extensions

import org.p2p.core.utils.Constants
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.moonpay.model.SellTransactionAmounts
import org.p2p.wallet.moonpay.model.SellTransactionMetadata
import org.p2p.wallet.moonpay.repository.sell.SellTransactionFiatCurrency
import org.p2p.wallet.utils.toBase58Instance
import java.math.BigDecimal

object SellStubs {
    private val EMPTY_METADATA = SellTransactionMetadata(
        createdAt = "",
        updatedAt = "",
        accountId = "",
        customerId = "",
        bankAccountId = "",
        externalTransactionId = "",
        externalCustomerId = "",
        countryAbbreviation = "",
        stateAbbreviation = ""
    )

    val EMPTY_WAITING_FOR_DEPOIST = SellTransaction.WaitingForDepositTransaction(
        metadata = EMPTY_METADATA,
        transactionId = "13123213",
        amounts = SellTransactionAmounts(
            tokenAmount = BigDecimal.ZERO,
            feeAmount = BigDecimal.ZERO,
            usdAmount = BigDecimal.ZERO,
            eurAmount = BigDecimal.ZERO,
            gbpAmount = BigDecimal.ZERO
        ),
        userAddress = Constants.SOL_MINT.toBase58Instance(),
        selectedFiat = SellTransactionFiatCurrency.USD,
        moonpayDepositWalletAddress = Constants.SOL_MINT.toBase58Instance(),
    )
}
