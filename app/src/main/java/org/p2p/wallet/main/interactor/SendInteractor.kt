package org.p2p.wallet.main.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.CheckAddressResult
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.TransactionResult
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.interactor.orca.TransactionAddressInteractor
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigInteger

class SendInteractor(
    private val rpcRepository: RpcRepository,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val addressInteractor: TransactionAddressInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    companion object {
        private const val SEND_TAG = "SEND"
    }

    /*
    * If transaction will need to create a new account,
    * then the fee for account creation will be paid via this token
    * */
    private lateinit var feePayerToken: Token.Active

    /*
    * Initialize fee payer token
    * */
    fun initialize(userTokens: List<Token.Active>) {
        val userPublicKey = tokenKeyProvider.publicKey.toPublicKey()
        feePayerToken = userTokens.first { it.isSOL && userPublicKey.equals(it.publicKey) }
    }

    fun setFeePayerToken(newToken: Token.Active) {
        if (!this::feePayerToken.isInitialized) throw IllegalStateException("PayToken is not initialized")
        if (newToken.publicKey.equals(feePayerToken)) return

        feePayerToken = newToken
    }

    suspend fun checkAddress(destinationAddress: PublicKey, token: Token.Active): CheckAddressResult = try {
        val address = addressInteractor.findAssociatedAddress(destinationAddress, token.mintAddress)
        if (address.shouldCreateAssociatedInstruction) {
            CheckAddressResult.NewAccountNeeded(feePayerToken)
        } else {
            CheckAddressResult.AccountExists
        }
    } catch (e: IllegalStateException) {
        CheckAddressResult.InvalidAddress
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
            addressInteractor.findAssociatedAddress(destinationAddress, token.mintAddress)
        } catch (e: IllegalStateException) {
            return TransactionResult.WrongWallet
        }

        if (currentUser == address.associatedAddress.toBase58()) {
            return TransactionResult.Error(R.string.main_send_to_yourself_error)
        }

        val userPublicKey = tokenKeyProvider.publicKey.toPublicKey()
        val feePayerPubkey = feeRelayerRepository.getPublicKey()

        val transaction = Transaction()
        val instructions = mutableListOf<TransactionInstruction>()

        if (address.shouldCreateAssociatedInstruction) {
            Timber.tag(SEND_TAG).d("Associated token account creation needed, adding create instruction")

            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                token.mintAddress.toPublicKey(),
                address.associatedAddress,
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
            address.associatedAddress,
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

        val recipientPubkey =
            if (!address.shouldCreateAssociatedInstruction || address.associatedAddress.equals(destinationAddress)) {
                address.associatedAddress.toBase58()
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
}