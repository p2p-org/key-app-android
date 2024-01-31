package org.p2p.wallet.send.repository

import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.network.data.EmptyDataException
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.send.api.SendServiceApi
import org.p2p.wallet.send.model.send_service.GeneratedTransaction
import org.p2p.wallet.send.model.send_service.SendFeePayerMode
import org.p2p.wallet.send.model.send_service.SendRentPayerMode
import org.p2p.wallet.send.model.send_service.SendTransferMode
import org.p2p.wallet.send.repository.SendServiceResponseMapper.toDomain

class SendServiceRemoteRepository(
    private val api: SendServiceApi,
    private val inMemoryRepository: SendServiceInMemoryRepository
) : SendServiceRepository {
    override suspend fun getCompensationTokens(): List<Base58String> {
        return try {
            val rpcRequest = RpcRequest("get_compensation_tokens", emptyList())
            val response = api.getCompensationTokens(rpcRequest)
            // add SOL manually, it can be used for compensation
            response.result.plus(Constants.WRAPPED_SOL_MINT.toBase58Instance())
        } catch (e: EmptyDataException) {
            Timber.i("`get_compensation_tokens` responded with empty data, returning null")
            emptyList()
        }
    }

    override suspend fun getMaxAmountToSend(
        userWallet: Base58String,
        recipient: Base58String,
        token: Token.Active
    ): BigInteger {
        val cachedFeeInfo = inMemoryRepository.getMaxAmountToSned(token.mintAddress.toBase58Instance())
        if (cachedFeeInfo != null) {
            return cachedFeeInfo
        }

        val generatedTransaction = generateTransaction(
            userWallet = userWallet,
            amountLamports = token.totalInLamports,
            recipient = recipient,
            tokenMint = if (token.isSOL) null else token.mintAddress.toBase58Instance(),
            // exactIn because we need to get totalAmount less than amount with send
            transferMode = SendTransferMode.ExactIn,
        )

        inMemoryRepository.putMaxAmountToSend(
            mintAddress = token.mintAddress.toBase58Instance(),
            maxAmount = generatedTransaction.recipientGetsAmount.amount
        )
        return generatedTransaction.recipientGetsAmount.amount
    }

    override suspend fun generateTransaction(
        userWallet: Base58String,
        amountLamports: BigInteger,
        recipient: Base58String,
        tokenMint: Base58String?,
        transferMode: SendTransferMode,
        feePayerMode: SendFeePayerMode,
        customFeePayerTokenMint: Base58String?,
        rentPayerMode: SendRentPayerMode,
        customRentPayerTokenMint: Base58String?,
    ): GeneratedTransaction {

        val params = buildMap {
            this += "user_wallet" to userWallet.base58Value
            this += "amount" to amountLamports.toString()
            this += "recipient" to recipient.base58Value
            if (tokenMint != null) {
                this += "mint" to tokenMint.base58Value
            }
            this += "options" to buildMap {
                this += "transfer_mode" to transferMode.name
                if (feePayerMode == SendFeePayerMode.Custom) {
                    require(customFeePayerTokenMint != null) {
                        "Custom fee payer token mint must be specified when fee payer mode is Custom"
                    }
                    this += "network_fee_payer" to customFeePayerTokenMint.base58Value
                } else {
                    this += "network_fee_payer" to feePayerMode.name
                }

                if (rentPayerMode == SendRentPayerMode.Custom) {
                    require(customRentPayerTokenMint != null) {
                        "Custom rent payer token mint must be specified when rent payer mode is Custom"
                    }
                    this += "ta_rent_payer" to customRentPayerTokenMint.base58Value
                } else {
                    this += "ta_rent_payer" to rentPayerMode.name
                }
            }
        }

        val rpcRequest = RpcMapRequest("transfer", params)
        return api.generateTransaction(rpcRequest).result.toDomain()
    }
}
