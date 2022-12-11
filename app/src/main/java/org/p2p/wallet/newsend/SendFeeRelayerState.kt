package org.p2p.wallet.newsend

import org.p2p.core.token.Token
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.toUsd
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import java.math.BigDecimal
import java.math.BigInteger

class SendFeeRelayerState(
    private val sendInteractor: SendInteractor
) {

    private var sendFeeTotal: SendFeeTotal? = null

    private lateinit var feeLimitInfo: FreeTransactionFeeLimit
    private lateinit var minRentExemption: BigInteger
    private lateinit var recipientAddress: SearchResult

    var onFeeUpdated: ((SendFeeTotal) -> Unit)? = null

    private var sendFee: SendSolanaFee? = null

    suspend fun initialize(initialToken: Token.Active, recipientAddress: SearchResult) {
        this.recipientAddress = recipientAddress

        sendInteractor.initialize(initialToken)
        feeLimitInfo = sendInteractor.getFreeTransactionsInfo()
        minRentExemption = sendInteractor.getMinRelayRentExemption()

        // Since the user didn't do anything on initialization yet
        calculateTotalFee(
            tokenAmount = BigDecimal.ZERO,
            usdAmount = BigDecimal.ZERO,
            sourceToken = initialToken
        )
    }

    fun getFeeTotal(): SendFeeTotal? = sendFeeTotal

    fun getSolanaFee(): SendSolanaFee? = sendFee

    fun getMinRentExemption(): BigInteger = minRentExemption

    private fun calculateTotalFee(
        tokenAmount: BigDecimal,
        usdAmount: BigDecimal,
        sourceToken: Token.Active
    ) {
        val total = SendFeeTotal(
            total = tokenAmount,
            totalUsd = usdAmount,
            receive = "${tokenAmount.formatToken()} ${sourceToken.tokenSymbol}",
            receiveUsd = tokenAmount.toUsd(sourceToken),
            fee = sendFee,
            sourceSymbol = sourceToken.tokenSymbol,
            recipientAddress = recipientAddress.addressState.address,
            feeLimit = feeLimitInfo
        )

        onFeeUpdated?.invoke(total)
    }
}
