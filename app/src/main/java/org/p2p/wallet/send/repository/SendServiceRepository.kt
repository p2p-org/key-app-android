package org.p2p.wallet.send.repository

import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.send.model.send_service.GeneratedTransaction
import org.p2p.wallet.send.model.send_service.SendFeePayerMode
import org.p2p.wallet.send.model.send_service.SendRentPayerMode
import org.p2p.wallet.send.model.send_service.SendTransferMode

interface SendServiceRepository {
    suspend fun getCompensationTokens(): List<Base58String>

    suspend fun generateTransactions(
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
}
