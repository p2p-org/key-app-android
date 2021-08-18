package com.p2p.wallet.main.interactor

import com.p2p.wallet.R
import com.p2p.wallet.common.crypto.Base64Utils
import com.p2p.wallet.history.model.TransactionConverter
import com.p2p.wallet.history.model.TransactionType
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.rpc.repository.FeeRelayerRepository
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionTypeParser
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.Transaction
import org.p2p.solanaj.model.core.TransactionInstruction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import timber.log.Timber
import java.math.BigInteger

class MainInteractor(
    private val rpcRepository: RpcRepository,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

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
            return TransactionResult.WrongWallet
        }

        val payer = tokenKeyProvider.publicKey.toPublicKey()

        val instructions = mutableListOf<TransactionInstruction>()
        val instruction = SystemProgram.transfer(
            fromPublicKey = payer,
            toPublickKey = destinationAddress,
            lamports = lamports.toLong()
        )
        instructions.add(instruction)

        val feePayerPublicKey = feeRelayerRepository.getPublicKey()
        val recentBlockHash = rpcRepository.getRecentBlockhash()

        val transaction = Transaction(
            feePayer = feePayerPublicKey,
            recentBlockhash = recentBlockHash.recentBlockhash,
            instructions = instructions
        )

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)

        val serializedMessage = transaction.serialize()
        val base64Trx: String = Base64Utils.encode(serializedMessage)
        Timber.d("### base64 $base64Trx")

        val signature = transaction.getSignature().orEmpty()

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
        token: Token,
        lamports: BigInteger
    ): TransactionResult {
        val currentUser = tokenKeyProvider.publicKey

        if (destinationAddress.toBase58().length < PublicKey.PUBLIC_KEY_LENGTH) {
            return TransactionResult.WrongWallet
        }

        val address = try {
            findSplTokenAddress(token.mintAddress, destinationAddress)
        } catch (e: IllegalStateException) {
            return TransactionResult.WrongWallet
        }

        if (currentUser == address.toBase58()) {
            return TransactionResult.Error(R.string.main_send_to_yourself_error)
        }

        val payer = tokenKeyProvider.publicKey.toPublicKey()
        val feePayerPubkey = feeRelayerRepository.getPublicKey()

        val instructions = mutableListOf<TransactionInstruction>()

        /* If account is not found, create one */
        val accountInfo = rpcRepository.getAccountInfo(address)
        val value = accountInfo?.value
        val associatedNotNeeded = value?.owner == TokenProgram.PROGRAM_ID.toString() && value.data != null
        if (!associatedNotNeeded) {
            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                mint = token.mintAddress.toPublicKey(),
                associatedAccount = address,
                owner = destinationAddress,
                payer = feePayerPubkey
            )

            instructions.add(createAccount)
        }

        val instruction = TokenProgram.createTransferCheckedInstruction(
            tokenProgramId = TokenProgram.PROGRAM_ID,
            source = token.publicKey.toPublicKey(),
            mint = token.mintAddress.toPublicKey(),
            destination = address,
            owner = payer,
            amount = lamports,
            decimals = token.decimals
        )

        instructions.add(instruction)

        val recentBlockHash = rpcRepository.getRecentBlockhash()

        val transaction = Transaction(
            feePayer = feePayerPubkey,
            recentBlockhash = recentBlockHash.recentBlockhash,
            instructions = instructions
        )

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)

        val serializedMessage = transaction.serialize()
        val base64Trx: String = Base64Utils.encode(serializedMessage)
        Timber.d("### serialized message: $base64Trx")

        val signature = transaction.getSignature().orEmpty()

        val transactionId = feeRelayerRepository.sendSplToken(
            senderTokenAccountPubkey = token.publicKey,
            recipientPubkey = address.toBase58(),
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

        val value = accountInfo?.value

        // create associated token address
        if (value == null || value.data?.get(0).isNullOrEmpty()) {
            return TokenTransaction.getAssociatedTokenAddress(mintAddress.toPublicKey(), destinationAddress)
        }

        val info = TokenTransaction.decodeAccountInfo(value)
        // detect if destination address is already a SPLToken address
        if (info.mint == destinationAddress) return destinationAddress

        // detect if destination address is a SOL address
        if (info.owner.toBase58() == TokenProgram.PROGRAM_ID.toBase58()) {

            // create associated token address
            return TokenTransaction.getAssociatedTokenAddress(mintAddress.toPublicKey(), destinationAddress)
        }

        throw IllegalStateException("Wallet address is not valid")
    }

    suspend fun getHistory(publicKey: String, before: String?, limit: Int): List<TransactionType> {
        val signatures = rpcRepository.getConfirmedSignaturesForAddress(
            publicKey.toPublicKey(), before, limit
        ).map { it.signature }

        return rpcRepository.getConfirmedTransactions(signatures)
            .mapNotNull { response ->
                val signature = response.transaction.signatures.firstOrNull()
                val data = TransactionTypeParser.parse(signature, response)

                val swap = data.firstOrNull { it is SwapDetails }
                if (swap != null) {
                    return@mapNotNull parseSwapDetails(swap as SwapDetails)
                }

                val transfer = data.firstOrNull { it is TransferDetails }
                if (transfer != null) {
                    return@mapNotNull parseTransferDetails(
                        transfer as TransferDetails,
                        publicKey,
                        tokenKeyProvider.publicKey
                    )
                }

                val close = data.firstOrNull { it is CloseAccountDetails }
                if (close != null) {
                    return@mapNotNull parseCloseDetails(close as CloseAccountDetails)
                }

                val unknown = data.firstOrNull { it is UnknownDetails }
                TransactionConverter.fromNetwork(unknown as UnknownDetails)
            }
            .sortedByDescending { it.date.toInstant().toEpochMilli() }
    }

    private fun parseSwapDetails(details: SwapDetails): TransactionType? {
        val sourceData = userLocalRepository.getTokenData(details.mintA) ?: return null
        val destinationData = userLocalRepository.getTokenData(details.mintB) ?: return null

        if (sourceData.mintAddress == destinationData.mintAddress) return null

        val destinationRate = userLocalRepository.getPriceByToken(destinationData.symbol)
        val source = tokenKeyProvider.publicKey
        return TransactionConverter.fromNetwork(details, sourceData, destinationData, destinationRate, source)
    }

    private fun parseTransferDetails(
        transfer: TransferDetails,
        directPublicKey: String,
        publicKey: String
    ): TransactionType {
        val symbol = if (transfer.isSimpleTransfer) Token.SOL_SYMBOL else findSymbol(transfer.mint)
        val rate = userLocalRepository.getPriceByToken(symbol)

        val mint = if (transfer.isSimpleTransfer) Token.SOL_MINT else transfer.mint
        val source = userLocalRepository.getTokenData(mint)!!

        return TransactionConverter.fromNetwork(transfer, source, directPublicKey, publicKey, rate)
    }

    private suspend fun parseCloseDetails(details: CloseAccountDetails): TransactionType {
        val accountInfo = rpcRepository.getAccountInfo(details.account.toPublicKey())
        val info = TokenTransaction.parseAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
        val symbol = findSymbol(info?.mint?.toBase58().orEmpty())
        return TransactionConverter.fromNetwork(details, symbol)
    }

    private fun findSymbol(mint: String): String =
        if (mint.isNotEmpty()) userLocalRepository.getTokenData(mint)?.symbol.orEmpty() else ""
}