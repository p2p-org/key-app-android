package org.p2p.wallet.send.repository

import org.p2p.wallet.send.api.responses.SendGeneratedTransactionResponse
import org.p2p.wallet.send.model.send_service.GeneratedTransaction

object SendServiceResponseMapper {
    fun SendGeneratedTransactionResponse.toDomain(): GeneratedTransaction = GeneratedTransaction(
        transaction = transaction,
        blockhash = blockhash,
        expiresAt = expiresAt,
        signature = signature,
        recipientGetsAmount = recipientGetsAmount.toDomain(),
        totalAmount = totalAmount.toDomain(),
        networkFee = networkFee.toDomain(),
        tokenAccountRent = tokenAccountRent?.toDomain() ?: GeneratedTransaction.NetworkFeeData.EMPTY,
        token2022TransferFee = token2022TransferFee?.toDomain() ?: GeneratedTransaction.NetworkFeeData.EMPTY,
    )

    fun SendGeneratedTransactionResponse.AmountResponse.toDomain(): GeneratedTransaction.AmountData =
        GeneratedTransaction.AmountData(
            amount = amount,
            usdAmount = usdAmount,
            address = address,
            tokenSymbol = tokenSymbol,
            tokenName = tokenName,
            decimals = decimals,
            logoUrl = logoUrl,
            coingeckoId = coingeckoId,
            price = price,
        )

    fun SendGeneratedTransactionResponse.NetworkFeeResponse.toDomain(): GeneratedTransaction.NetworkFeeData =
        GeneratedTransaction.NetworkFeeData(
            source = source,
            amount = amount.toDomain(),
        )
}
