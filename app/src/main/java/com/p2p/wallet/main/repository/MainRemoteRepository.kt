package com.p2p.wallet.main.repository

import com.p2p.wallet.common.date.toZonedDateTime
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Utils
import org.p2p.solanaj.data.RpcRepository
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.TransactionRequest
import org.p2p.solanaj.model.types.ConfirmedTransaction
import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.solanaj.model.types.TransferInfoResponse
import org.p2p.solanaj.programs.SystemProgram

class MainRemoteRepository(
    private val tokenProvider: TokenKeyProvider,
    private val rpcRepository: RpcRepository
) : MainRepository {

    override suspend fun sendToken(
        blockhash: RecentBlockhash,
        targetAddress: String,
        lamports: Long,
        tokenSymbol: String
    ): String {

        val sourcePublicKey = tokenProvider.publicKey.toPublicKey()
        val targetPublicKey = targetAddress.toPublicKey()
        val signer = Account(tokenProvider.secretKey)

        val transaction = TransactionRequest()
        val instruction = SystemProgram.transfer(
            sourcePublicKey,
            targetPublicKey,
            lamports
        )
        transaction.addInstruction(instruction)

        return rpcRepository.sendTransaction(blockhash, transaction, listOf(signer))
    }

    override suspend fun getRecentBlockhash(): RecentBlockhash =
        rpcRepository.getRecentBlockhash()

    /* TODO: will be refactored */
    override suspend fun getHistory(publicKey: String, tokenSymbol: String, limit: Int): List<Transaction> =
        withContext(Dispatchers.IO) {
            val signatures = rpcRepository.getConfirmedSignaturesForAddress2(publicKey.toPublicKey(), limit)

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

    private suspend fun getConfirmedTransaction(signature: String, slot: Long): TransferInfoResponse? {
        val trx = rpcRepository.getConfirmedTransaction(signature)
        val message: ConfirmedTransaction.Message = trx.transaction.message
        val meta: ConfirmedTransaction.Meta = trx.meta
        val instructions = message.instructions

        for (instruction in instructions) {
            if (message.accountKeys[instruction.programIdIndex.toInt()] == tokenProvider.ownerKey) {
                val data = Base58.decode(instruction.data)
                val lamports = Utils.readInt64(data, 4)

                return TransferInfoResponse(
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

                    return TransferInfoResponse(
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
        return null
    }

    private suspend fun getBlockTime(slot: Long) =
        rpcRepository.getBlockTime(slot)
}