package com.p2p.wallet.main.repository

import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Utils
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.rpc.RpcClient
import org.p2p.solanaj.rpc.types.ConfirmedTransaction
import org.p2p.solanaj.rpc.types.TransferInfo

class MainRemoteRepository(
    private val client: RpcClient,
    private val tokenProvider: TokenKeyProvider
) : MainRepository {

    override suspend fun sendToken(targetAddress: String, lamports: Long, tokenSymbol: String): String =
        withContext(Dispatchers.IO) {
            val sourcePublicKey = PublicKey(tokenProvider.publicKey)
            val targetPublicKey = PublicKey(targetAddress)
            val signer = Account()

            val transaction = Transaction()
            transaction.addInstruction(
                SystemProgram.transfer(
                    sourcePublicKey,
                    targetPublicKey,
                    lamports
                )
            )

            client.api.sendTransaction(transaction, signer)
        }

    override suspend fun getTransaction(signature: String, slot: Long): TransferInfo? = withContext(Dispatchers.IO) {
        val trx = client.api.getConfirmedTransaction(signature)
        val message: ConfirmedTransaction.Message = trx.transaction.message
        val meta: ConfirmedTransaction.Meta = trx.meta
        val instructions: List<ConfirmedTransaction.Instruction> = message.instructions

        instructions.forEach {
            if (message.accountKeys[it.programIdIndex.toInt()] == "11111111111111111111111111111111") {
                val data = Base58.decode(it.data)
                val lamports = Utils.readInt64(data, 4)

                val transferInfo = TransferInfo(
                    message.accountKeys[it.accounts[0].toInt()],
                    message.accountKeys[it.accounts[1].toInt()],
                    lamports
                )
                transferInfo.slot = slot
                transferInfo.signature = signature
                transferInfo.setFee(meta.fee)
                transferInfo
            } else {
                if (message.accountKeys[it.programIdIndex.toInt()] == tokenProvider.programPublicKey) {
                    val data = Base58.decode(it.data)
                    val lamports = Utils.readInt64(data, 1)

                    val transferInfo = TransferInfo(
                        message.accountKeys[it.accounts[0].toInt()],
                        message.accountKeys[it.accounts[1].toInt()],
                        lamports
                    )
                    transferInfo.slot = slot
                    transferInfo.signature = signature
                    transferInfo.setFee(meta.fee)
                    transferInfo
                }
            }
        }
        null
    }
}