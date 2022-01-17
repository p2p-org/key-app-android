package org.p2p.wallet.main.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.TransactionResult
import org.p2p.wallet.rpc.repository.FeeRelayerRepository
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.interactor.orca.OrcaAddressInteractor
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigInteger

class SendInteractor(
    private val rpcRepository: RpcRepository,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val addressInteractor: OrcaAddressInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    companion object {
        private const val SEND_TAG = "SEND"
    }

    suspend fun sendNativeSolToken(
        destinationAddress: PublicKey,
        lamports: BigInteger
    ): TransactionResult {
        val currentUser = tokenKeyProvider.publicKey

        if (currentUser == destinationAddress.toBase58()) {
            return TransactionResult.Error(R.string.main_send_to_yourself_error)
        }

        val accountInfo = rpcRepository.getAccountInfo(destinationAddress.toBase58())
        val value = accountInfo?.value

        if (value?.owner == TokenProgram.PROGRAM_ID.toBase58()) {
            Timber.tag(SEND_TAG).d("Owner address matches the program id, returning with error")
            return TransactionResult.WrongWallet
        }

        val payer = tokenKeyProvider.publicKey.toPublicKey()

        val transaction = Transaction()
        val instructions = mutableListOf<TransactionInstruction>()

        val instruction = SystemProgram.transfer(
            fromPublicKey = payer,
            toPublicKey = destinationAddress,
            lamports = lamports
        )
        transaction.addInstruction(instruction)
        instructions += instruction

        val feePayerPublicKey = feeRelayerRepository.getPublicKey()
        val recentBlockHash = rpcRepository.getRecentBlockhash()

        transaction.setFeePayer(feePayerPublicKey)
        transaction.setRecentBlockHash(recentBlockHash.recentBlockhash)

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)

        val result = feeRelayerRepository.send(
            instructions = instructions,
            signatures = transaction.allSignatures,
            pubkeys = transaction.accountKeys,
            blockHash = recentBlockHash.recentBlockhash
        ).firstOrNull().orEmpty()

        return TransactionResult.Success(result)
    }

    suspend fun sendSplToken(
        destinationAddress: PublicKey,
        token: Token.Active,
        lamports: BigInteger
    ): TransactionResult {
        val currentUser = tokenKeyProvider.publicKey

        if (destinationAddress.toBase58().length < PublicKey.PUBLIC_KEY_LENGTH) {
            return TransactionResult.WrongWallet
        }

        val address = try {
            Timber.tag(SEND_TAG).d("Searching for SPL token address")
            addressInteractor.findSplTokenAddress(token.mintAddress, destinationAddress)
        } catch (e: IllegalStateException) {
            Timber.tag(SEND_TAG).d("Searching address failed, address is wrong")
            return TransactionResult.WrongWallet
        }

        if (currentUser == address.toBase58()) {
            return TransactionResult.Error(R.string.main_send_to_yourself_error)
        }

        val userPublicKey = tokenKeyProvider.publicKey.toPublicKey()
        val feePayerPubkey = feeRelayerRepository.getPublicKey()

        val transaction = Transaction()
        val instructions = mutableListOf<TransactionInstruction>()

        /* If account is not found, create one */
        val accountInfo = rpcRepository.getAccountInfo(address.toBase58())
        val value = accountInfo?.value
        val associatedNotNeeded = value?.owner == TokenProgram.PROGRAM_ID.toString() && value.data != null
        if (!associatedNotNeeded) {
            Timber.tag(SEND_TAG).d("Associated token account creation needed, adding create instruction")

            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                token.mintAddress.toPublicKey(),
                address,
                destinationAddress,
                userPublicKey
            )

            transaction.addInstruction(createAccount)
            instructions += createAccount
        }

        val instruction = TokenProgram.createTransferCheckedInstruction(
            TokenProgram.PROGRAM_ID,
            token.publicKey.toPublicKey(),
            token.mintAddress.toPublicKey(),
            address,
            userPublicKey,
            lamports,
            token.decimals
        )

        transaction.addInstruction(instruction)
        instructions += instruction

        val recentBlockHash = rpcRepository.getRecentBlockhash()

        transaction.setFeePayer(feePayerPubkey)
        transaction.setRecentBlockHash(recentBlockHash.recentBlockhash)

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)

        val recipientPubkey = if (associatedNotNeeded || address.equals(destinationAddress)) {
            address.toBase58()
        } else {
            destinationAddress.toBase58()
        }

        Timber.tag(SEND_TAG).d("Recipient's address is $recipientPubkey")

        val transactionId = feeRelayerRepository.send(
            instructions = instructions,
            signatures = transaction.allSignatures,
            pubkeys = transaction.accountKeys,
            blockHash = recentBlockHash.recentBlockhash
        ).firstOrNull().orEmpty()

        return TransactionResult.Success(transactionId)
    }
}