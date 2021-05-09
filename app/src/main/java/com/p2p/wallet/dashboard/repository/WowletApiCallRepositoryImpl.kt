package com.p2p.wallet.dashboard.repository

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionResponse
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.rpc.RpcClient
import org.p2p.solanaj.rpc.RpcException

@Deprecated("this will be deleted")
class WowletApiCallRepositoryImpl(
    private val client: RpcClient
) : WowletApiCallRepository {

    override suspend fun getMinimumBalance(accountLenght: Long): Long {
        val minimumBalance: Long =
            client.api.getMinimumBalanceForRentExemption(accountLenght)
        return minimumBalance
    }

    override suspend fun getFee(): Long = client.api.feeBlockhash

    @Throws(RpcException::class)
    override suspend fun createAndInitializeTokenAccount(
        payer: Account,
        mintAddress: PublicKey,
        newAccount: Account
    ): String {
        val space = (32 + 32 + 8 + 93).toLong() // mint account data length: 32 + 32 + 8 + 93
        val newAccountPubKey = newAccount.publicKey
        val payerPubKey = payer.publicKey
        val minBalance = client.api.getMinimumBalanceForRentExemption(space)
        val createAccount = SystemProgram.createAccount(
            payerPubKey, newAccountPubKey, minBalance,
            space, SystemProgram.SPL_TOKEN_PROGRAM_ID
        )
        val initializeAccount = SystemProgram.initializeAccountInstruction(
            newAccountPubKey, mintAddress,
            payerPubKey
        )
        val transaction = TransactionResponse()
        transaction.addInstruction(createAccount)
        transaction.addInstruction(initializeAccount)
        return client.api.sendTransaction(transaction, listOf(payer, newAccount))
    }
}