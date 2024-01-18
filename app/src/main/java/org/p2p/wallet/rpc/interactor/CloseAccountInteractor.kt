package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.rpc.repository.history.RpcTransactionRepository
import org.p2p.wallet.utils.toPublicKey

class CloseAccountInteractor(
    private val rpcBlockhashRepository: RpcBlockhashRepository,
    private val rpcTransactionRepository: RpcTransactionRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    // todo: check programId !!
    suspend fun close(
        addressToClose: String,
        programId: PublicKey = TokenProgram.PROGRAM_ID,
    ): String {
        val owner = tokenKeyProvider.publicKey.toPublicKey()
        val instruction = TokenProgram.closeAccountInstruction(
            programId,
            addressToClose.toPublicKey(),
            owner,
            owner
        )

        val transaction = Transaction()
        transaction.addInstruction(instruction)

        val recentBlockhash = rpcBlockhashRepository.getRecentBlockhash()
        transaction.setRecentBlockhash(recentBlockhash.recentBlockhash)

        val signers = Account(tokenKeyProvider.keyPair)
        transaction.sign(signers)

        return rpcTransactionRepository.sendTransaction(transaction)
    }
}
