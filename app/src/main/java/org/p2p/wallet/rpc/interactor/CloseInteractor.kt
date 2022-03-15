package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockHashRepository
import org.p2p.wallet.rpc.repository.transaction.RpcTransactionRepository
import org.p2p.wallet.utils.toPublicKey

class CloseInteractor(
    private val rpcBlockHashRepository: RpcBlockHashRepository,
    private val rpcTransactionRepository: RpcTransactionRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun close(addressToClose: String): String {
        val owner = tokenKeyProvider.publicKey.toPublicKey()
        val instruction = TokenProgram.closeAccountInstruction(
            TokenProgram.PROGRAM_ID,
            addressToClose.toPublicKey(),
            owner,
            owner
        )

        val transaction = Transaction()
        transaction.addInstruction(instruction)

        val recentBlockhash = rpcBlockHashRepository.getRecentBlockhash()
        transaction.recentBlockHash = recentBlockhash.recentBlockhash

        val signers = Account(tokenKeyProvider.secretKey)
        transaction.sign(signers)

        return rpcTransactionRepository.sendTransaction(transaction)
    }
}