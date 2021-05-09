package com.p2p.wallet.main.repository

import com.p2p.wallet.common.date.toZonedDateTime
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.token.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Utils
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionResponse
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.rpc.RpcClient
import org.p2p.solanaj.rpc.types.ConfirmedTransaction
import org.p2p.solanaj.rpc.types.TransferInfoResponse

class MainRemoteRepository(
    private val client: RpcClient,
    private val tokenProvider: TokenKeyProvider
) : MainRepository {

    override suspend fun sendToken(targetAddress: String, lamports: Long, tokenSymbol: String): String =
        withContext(Dispatchers.IO) {
            val sourcePublicKey = PublicKey(tokenProvider.publicKey)
            val targetPublicKey = PublicKey(targetAddress)
            val signer = Account(tokenProvider.secretKey)

            val transaction = TransactionResponse()
            transaction.addInstruction(
                SystemProgram.transfer(
                    sourcePublicKey,
                    targetPublicKey,
                    lamports
                )
            )

            client.api.sendTransaction(transaction, signer)
        }

    override suspend fun getHistory(depositAddress: String, tokenSymbol: String, limit: Int): List<Transaction> =
        withContext(Dispatchers.IO) {
            val signatures = client.api.getConfirmedSignaturesForAddress2(PublicKey(depositAddress), limit)

            return@withContext signatures
                .map {
                    async { getConfirmedTransaction(it.signature, it.slot.toLong()) }
                }
                .mapNotNull {
                    val response = it.await() ?: return@mapNotNull null
                    val dateRequest = async { getBlockTime(response.slot) }
                    response to dateRequest
                }
                .map { (response, dateRequest) ->
                    TokenConverter.fromNetwork(
                        response,
                        tokenProvider.publicKey,
                        tokenSymbol,
                        dateRequest.await().toZonedDateTime()
                    )
                }
                .sortedByDescending { it.date.toInstant().toEpochMilli() }
        }

    private suspend fun getConfirmedTransaction(signature: String, slot: Long): TransferInfoResponse? =
        withContext(Dispatchers.IO) {
            val trx = client.api.getConfirmedTransaction(signature)
            val message: ConfirmedTransaction.Message = trx.transaction.message
            val meta: ConfirmedTransaction.Meta = trx.meta
            val instructions = message.instructions

            for (instruction in instructions) {
                if (message.accountKeys[instruction.programIdIndex.toInt()] == tokenProvider.ownerKey) {
                    val data = Base58.decode(instruction.data)
                    val lamports = Utils.readInt64(data, 4)

                    return@withContext TransferInfoResponse(
                        message.accountKeys[instruction.accounts[0].toInt()],
                        message.accountKeys[instruction.accounts[1].toInt()],
                        lamports,
                        slot,
                        signature,
                        meta.fee
                    )
                } else {
                    if (message.accountKeys[instruction.programIdIndex.toInt()] == tokenProvider.rpcPublicKey) {
                        val data = Base58.decode(instruction.data)
                        val lamports = Utils.readInt64(data, 1)

                        return@withContext TransferInfoResponse(
                            message.accountKeys[instruction.accounts[0].toInt()],
                            message.accountKeys[instruction.accounts[1].toInt()],
                            lamports,
                            slot,
                            signature,
                            meta.fee
                        )
                    }
                }
            }
            return@withContext null
        }

    private suspend fun getBlockTime(slot: Long) = withContext(Dispatchers.IO) {
        client.api.getBlockTime(slot)
    }
}