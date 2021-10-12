package com.p2p.wallet.main.interactor

import com.p2p.wallet.R
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.rpc.repository.FeeRelayerRepository
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import timber.log.Timber
import java.math.BigInteger

class SendInteractor(
    private val rpcRepository: RpcRepository,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val userLocalRepository: UserLocalRepository,
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

        val accountInfo = rpcRepository.getAccountInfo(destinationAddress)
        val value = accountInfo?.value

        if (value?.owner == TokenProgram.PROGRAM_ID.toBase58()) {
            Timber.tag(SEND_TAG).d("Owner address matches the program id, returning with error")
            return TransactionResult.WrongWallet
        }

        val payer = tokenKeyProvider.publicKey.toPublicKey()

        val transaction = Transaction()

        val instruction = SystemProgram.transfer(
            fromPublicKey = payer,
            toPublickKey = destinationAddress,
            lamports = lamports.toLong()
        )
        transaction.addInstruction(instruction)

        val feePayerPublicKey = feeRelayerRepository.getPublicKey()
        val recentBlockHash = rpcRepository.getRecentBlockhash()

        transaction.setFeePayer(feePayerPublicKey)
        transaction.setRecentBlockHash(recentBlockHash.recentBlockhash)

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)

        val signature = transaction.signature.orEmpty()

        val result = feeRelayerRepository.sendSolToken(
            senderPubkey = tokenKeyProvider.publicKey,
            recipientPubkey = destinationAddress.toBase58(),
            lamports = lamports,
            signature = signature,
            blockhash = recentBlockHash.recentBlockhash
        )

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
            findSplTokenAddress(token.mintAddress, destinationAddress)
        } catch (e: IllegalStateException) {
            Timber.tag(SEND_TAG).d("Searching address failed, address is wrong")
            return TransactionResult.WrongWallet
        }

        if (currentUser == address.toBase58()) {
            return TransactionResult.Error(R.string.main_send_to_yourself_error)
        }

        val payer = tokenKeyProvider.publicKey.toPublicKey()
        val feePayerPubkey = feeRelayerRepository.getPublicKey()

        val transaction = Transaction()

        /* If account is not found, create one */
        val accountInfo = rpcRepository.getAccountInfo(address)
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
                feePayerPubkey
            )

            transaction.addInstruction(createAccount)
        }

        val instruction = TokenProgram.createTransferCheckedInstruction(
            TokenProgram.PROGRAM_ID,
            token.publicKey.toPublicKey(),
            token.mintAddress.toPublicKey(),
            address,
            payer,
            lamports,
            token.decimals
        )

        transaction.addInstruction(instruction)

        val recentBlockHash = rpcRepository.getRecentBlockhash()

        transaction.setFeePayer(feePayerPubkey)
        transaction.setRecentBlockHash(recentBlockHash.recentBlockhash)

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)

        val signature = transaction.signature.orEmpty()

        val recipientPubkey = if (associatedNotNeeded || address.equals(destinationAddress)) {
            address.toBase58()
        } else {
            destinationAddress.toBase58()
        }

        Timber.tag(SEND_TAG).d("Recipient's address is $recipientPubkey")

        val transactionId = feeRelayerRepository.sendSplToken(
            senderTokenAccountPubkey = token.publicKey,
            recipientPubkey = recipientPubkey,
            tokenMintPubkey = token.mintAddress,
            authorityPubkey = tokenKeyProvider.publicKey,
            lamports = lamports,
            decimals = token.decimals,
            signature = signature,
            blockhash = recentBlockHash.recentBlockhash
        )

        return TransactionResult.Success(transactionId)
    }

    private suspend fun findSplTokenAddress(mintAddress: String, destinationAddress: PublicKey): PublicKey {
        val accountInfo = rpcRepository.getAccountInfo(destinationAddress)

        // detect if it is a direct token address
        val info = TokenTransaction.parseAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
        if (info != null && userLocalRepository.findTokenData(info.mint.toBase58()) != null) {
            Timber.tag(SEND_TAG).d("Token by mint was found. Continuing with direct address")
            return destinationAddress
        }

        // create associated token address
        val value = accountInfo?.value
        if (value == null || value.data?.get(0).isNullOrEmpty()) {
            Timber.tag(SEND_TAG).d("No information found, creating associated token address")
            return TokenTransaction.getAssociatedTokenAddress(mintAddress.toPublicKey(), destinationAddress)
        }

        // detect if destination address is already a SPLToken address
        if (info?.mint == destinationAddress) {
            Timber.tag(SEND_TAG).d("Destination address is already an SPL Token address, returning")
            return destinationAddress
        }

        // detect if destination address is a SOL address
        if (info?.owner?.toBase58() == TokenProgram.PROGRAM_ID.toBase58()) {
            Timber.tag(SEND_TAG).d("Destination address is SOL address. Getting the associated token address")

            // create associated token address
            return TokenTransaction.getAssociatedTokenAddress(mintAddress.toPublicKey(), destinationAddress)
        }

        throw IllegalStateException("Wallet address is not valid")
    }
}