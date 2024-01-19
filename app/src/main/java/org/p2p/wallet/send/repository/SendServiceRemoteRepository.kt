package org.p2p.wallet.send.repository

import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.network.data.EmptyDataException
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.send.api.SendServiceApi
import org.p2p.wallet.send.api.responses.SendServiceFreeLimitsResponse
import org.p2p.wallet.send.model.send_service.GeneratedTransaction
import org.p2p.wallet.send.model.send_service.SendFeePayerMode
import org.p2p.wallet.send.model.send_service.SendRentPayerMode
import org.p2p.wallet.send.model.send_service.SendTransferMode
import org.p2p.wallet.send.repository.SendServiceResponseMapper.toDomain

class SendServiceRemoteRepository(
    private val api: SendServiceApi
) : SendServiceRepository {
    override suspend fun getCompensationTokens(): List<Base58String> {
        return try {
            val rpcRequest = RpcRequest("get_compensation_tokens", emptyList())
            val response = api.getCompensationTokens(rpcRequest)
            response.result
        } catch (e: EmptyDataException) {
            Timber.i("`get_compensation_tokens` responded with empty data, returning null")
            emptyList()
        }
    }

    override suspend fun getTokenAccountRentExempt(mintAddresses: List<Base58String>): Map<Base58String, BigInteger> {
        val params = buildMap {
            this += "mints" to mintAddresses.map { it.base58Value }
        }
        val rpcRequest = RpcMapRequest("get_token_account_rent_exempt", params)
        return api.getTokenAccountRentExempt(rpcRequest).result
    }

    override suspend fun getFeeLimits(userWallet: Base58String): SendServiceFreeLimitsResponse {
        val params = buildMap {
            this += "user_wallet" to userWallet.base58Value
        }
        val rpcRequest = RpcMapRequest("limits", params)
        return api.getLimits(rpcRequest).result
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
