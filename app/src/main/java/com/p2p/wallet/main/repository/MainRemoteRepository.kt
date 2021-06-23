package com.p2p.wallet.main.repository

import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.rpc.RpcRepository
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.TransactionTypeParser
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.TransactionRequest
import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.solanaj.programs.SystemProgram

class MainRemoteRepository(
    private val tokenProvider: TokenKeyProvider,
    private val rpcRepository: RpcRepository,
    private val userLocalRepository: UserLocalRepository
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

    override suspend fun getHistory(publicKey: String, tokenSymbol: String, limit: Int): List<Transaction> {
        val signatures = rpcRepository.getConfirmedSignaturesForAddress2(publicKey.toPublicKey(), limit)

        return signatures
            .map { signature ->
                val data = getConfirmedTransaction(signature.signature)
                val swap = data.firstOrNull { it.type == TransactionDetailsType.SWAP }
                val transfer = data.firstOrNull { it.type == TransactionDetailsType.TRANSFER }
                val close = data.firstOrNull { it.type == TransactionDetailsType.CLOSE_ACCOUNT }
                val unknown = data.firstOrNull { it.type == TransactionDetailsType.UNKNOWN }

                val (details, symbol) = when {
                    swap != null -> swap to ""
                    transfer != null -> transfer to findSymbol((transfer as TransferDetails).mint)
                    close != null -> close to ""
                    else -> unknown to ""
                }

                TokenConverter.fromNetwork(details, publicKey, symbol)
            }
            .sortedByDescending { it.date.toInstant().toEpochMilli() }
    }

    private suspend fun getConfirmedTransaction(signature: String): List<TransactionDetails> =
        withContext(Dispatchers.Default) {
            val response = rpcRepository.getConfirmedTransaction(signature)
            return@withContext TransactionTypeParser.parse(response)
        }

    private fun findSymbol(mint: String): String =
        userLocalRepository.getTokenData(mint)?.symbol.orEmpty()
}