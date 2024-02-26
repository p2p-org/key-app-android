package org.p2p.wallet.send.repository

import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.wallet.send.model.SendServiceFees
import org.p2p.wallet.send.model.send_service.GeneratedTransaction
import org.p2p.wallet.send.model.send_service.SendFeePayerMode
import org.p2p.wallet.send.model.send_service.SendRentPayerMode
import org.p2p.wallet.send.model.send_service.SendTransferMode

class SendServiceTransactionError(
    message: String,
    val code: Int,
) : Exception(message)

interface SendServiceRepository {
    companion object {
        val UINT64_MAX = BigInteger("18446744073709551615")
    }

    suspend fun getCompensationTokens(): List<Base58String>

    suspend fun generateTransaction(
        userWallet: Base58String,
        amountLamports: BigInteger,
        recipient: Base58String,
        tokenMint: Base58String? = null,
        transferMode: SendTransferMode = SendTransferMode.ExactOut,
        feePayerMode: SendFeePayerMode = SendFeePayerMode.UserSol,
        customFeePayerTokenMint: Base58String? = null,
        rentPayerMode: SendRentPayerMode = SendRentPayerMode.UserSol,
        customRentPayerTokenMint: Base58String? = null,
    ): GeneratedTransaction

    suspend fun getMaxAmountToSend(
        userWallet: Base58String,
        recipient: Base58String,
        token: Token.Active
    ): BigInteger

    suspend fun getTokenRentExemption(token: Token.Active): BigInteger

    suspend fun getTokenRentExemption(tokenMint: Base58String): BigInteger

    suspend fun estimateFees(
        userWallet: Base58String,
        recipient: Base58String,
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        amount: BigInteger
    ): SendServiceFees
}
