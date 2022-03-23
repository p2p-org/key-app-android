package org.p2p.wallet.rpc.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.rpc.repository.history.RpcHistoryRepository
import org.p2p.wallet.utils.toPublicKey

class TokenInteractor(
    private val rpcRepository: RpcHistoryRepository,
    private val rpcBlockhashRepository: RpcBlockhashRepository,
    private val addressInteractor: TransactionAddressInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun closeTokenAccount(addressToClose: String): String {
        val owner = tokenKeyProvider.publicKey.toPublicKey()
        val instruction = TokenProgram.closeAccountInstruction(
            TokenProgram.PROGRAM_ID,
            addressToClose.toPublicKey(),
            owner,
            owner
        )

        val transaction = Transaction()
        transaction.addInstruction(instruction)

        val recentBlockhash = rpcBlockhashRepository.getRecentBlockhash()
        transaction.recentBlockHash = recentBlockhash.recentBlockhash

        val signers = Account(tokenKeyProvider.secretKey)
        transaction.sign(signers)

        return rpcRepository.sendTransaction(transaction)
    }

    suspend fun openTokenAccount(
        mintAddress: String
    ): String {

        val account = Account(tokenKeyProvider.secretKey)
        val feePayer = account.publicKey

        val splDestinationAddress = addressInteractor.findSplTokenAddressData(
            destinationAddress = tokenKeyProvider.publicKey.toPublicKey(),
            mintAddress = mintAddress
        )

        val toPublicKey = splDestinationAddress.destinationAddress
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        val instruction = TokenProgram.createAssociatedTokenAccountInstruction(
            TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
            TokenProgram.PROGRAM_ID,
            mintAddress.toPublicKey(),
            toPublicKey,
            owner,
            feePayer
        )

        val signers = Account(tokenKeyProvider.secretKey)
        val recentBlockHash = rpcBlockhashRepository.getRecentBlockhash()

        val transaction = Transaction().apply {
            addInstruction(instruction)
            setRecentBlockHash(recentBlockHash.recentBlockhash)
            sign(signers)
        }

        return rpcRepository.sendTransaction(transaction)
    }
}
