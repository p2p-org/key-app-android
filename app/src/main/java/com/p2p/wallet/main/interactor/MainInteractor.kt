package com.p2p.wallet.main.interactor

import com.p2p.wallet.R
import com.p2p.wallet.common.crypto.Base64Utils
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.rpc.repository.FeeRelayerRepository
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.Transaction
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
import org.p2p.solanaj.model.core.TransactionRequest
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

    suspend fun sendTransaction(
        ownerAddress: PublicKey,
        token: Token,
        lamports: BigInteger
    ): TransactionResult {
        val currentUser = tokenKeyProvider.publicKey

        if (currentUser == ownerAddress.toBase58()) {
            return TransactionResult.Error(R.string.main_send_to_yourself_error)
        }

        if (ownerAddress.toBase58().length < PublicKey.PUBLIC_KEY_LENGTH) {
            return TransactionResult.WrongWallet
        }

        val transaction = TransactionRequest()
        val accountInfo = rpcRepository.getAccountInfo(ownerAddress)

        val info = TokenTransaction.parseAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
        val address = if (info != null && userLocalRepository.getTokenData(info.mint.toBase58()) != null) {
            Timber.d("Token by mint was found. Continuing with direct address")
            ownerAddress
        } else {
            Timber.d("No token data found, getting associated token address")
            TokenTransaction.getAssociatedTokenAddress(token.mintAddress.toPublicKey(), ownerAddress)
        }

        val payer = tokenKeyProvider.publicKey.toPublicKey()

        /* If account is not found, create one */
        if (!token.isSOL && accountInfo?.value == null) {
            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                associatedProgramId = TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                tokenProgramId = TokenProgram.PROGRAM_ID,
                mint = token.mintAddress.toPublicKey(),
                associatedAccount = address,
                owner = ownerAddress,
                payer = payer
            )

            transaction.addInstruction(createAccount)
        }

        if (token.isSOL) {
            val instruction = SystemProgram.transfer(
                fromPublicKey = payer,
                toPublickKey = address,
                lamports = lamports.toLong()
            )
            transaction.addInstruction(instruction)
        } else {
            val instruction = TokenProgram.createTransferCheckedInstruction(
                tokenProgramId = TokenProgram.PROGRAM_ID,
                source = token.publicKey.toPublicKey(),
                destination = address,
                owner = payer,
                amount = lamports,
                mint = token.mintAddress.toPublicKey(),
                decimals = token.decimals
            )

            transaction.addInstruction(instruction)
        }

        val recentBlockHash = rpcRepository.getRecentBlockhash()
        transaction.setRecentBlockHash(recentBlockHash.recentBlockhash)

        val feePayerPubkey = feeRelayerRepository.getPublicKey()
        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.setFeePayer(feePayerPubkey.toPublicKey())
        transaction.sign(signers)
        val signature = transaction.getSignature().orEmpty()

        val serializedTransaction = transaction.serialize()

        val base64Trx: String = Base64Utils.encode(serializedTransaction)
        Timber.d("### data $base64Trx")

        val result = if (token.isSOL) {
            feeRelayerRepository.sendSolToken(
                tokenKeyProvider.publicKey,
                address.toBase58(),
                lamports,
                signature,
                recentBlockHash.recentBlockhash
            )
        } else {
            feeRelayerRepository.sendSplToken(
                token.publicKey,
                address.toBase58(),
                token.mintAddress,
                tokenKeyProvider.publicKey,
                lamports,
                token.decimals,
                signature,
                recentBlockHash.recentBlockhash
            )
        }

        return TransactionResult.Success(result)
    }

    suspend fun getHistory(publicKey: String, before: String?, limit: Int): List<Transaction> {
        val signatures = rpcRepository.getConfirmedSignaturesForAddress(
            publicKey.toPublicKey(), before, limit
        ).map { it.signature }

        return rpcRepository.getConfirmedTransactions(signatures)
            .mapNotNull { response ->
                val signature = response.transaction.signatures.firstOrNull()
                val data = TransactionTypeParser.parse(signature, response)

                val swap = data.firstOrNull { it is SwapDetails }
                val transfer = data.firstOrNull { it is TransferDetails }
                val close = data.firstOrNull { it is CloseAccountDetails }
                val unknown = data.firstOrNull { it is UnknownDetails }

                return@mapNotNull when {
                    swap != null -> parseSwapDetails(swap as SwapDetails)
                    transfer != null -> parseTransferDetails(
                        transfer as TransferDetails,
                        publicKey,
                        tokenKeyProvider.publicKey
                    )
                    close != null -> parseCloseDetails(close as CloseAccountDetails)
                    else -> TokenConverter.fromNetwork(unknown as UnknownDetails)
                }
            }
            .sortedByDescending { it.date.toInstant().toEpochMilli() }
    }

    private fun parseSwapDetails(details: SwapDetails): Transaction? {
        val sourceData = userLocalRepository.getTokenData(details.mintA) ?: return null
        val destinationData = userLocalRepository.getTokenData(details.mintB) ?: return null

        if (sourceData.mintAddress == destinationData.mintAddress) return null

        val destinationRate = userLocalRepository.getPriceByToken(destinationData.symbol)
        return TokenConverter.fromNetwork(details, sourceData, destinationData, destinationRate)
    }

    private fun parseTransferDetails(
        transfer: TransferDetails,
        directPublicKey: String,
        publicKey: String
    ): Transaction {
        val symbol = if (transfer.isSimpleTransfer) Token.SOL_SYMBOL else findSymbol(transfer.mint)
        val rate = userLocalRepository.getPriceByToken(symbol)

        return TokenConverter.fromNetwork(transfer, directPublicKey, publicKey, rate, symbol)
    }

    private suspend fun parseCloseDetails(details: CloseAccountDetails): Transaction {
        val accountInfo = rpcRepository.getAccountInfo(details.account.toPublicKey())
        val info = TokenTransaction.parseAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
        val symbol = findSymbol(info?.mint?.toBase58().orEmpty())
        return TokenConverter.fromNetwork(details, symbol)
    }

    private fun findSymbol(mint: String): String =
        if (mint.isNotEmpty()) userLocalRepository.getTokenData(mint)?.symbol.orEmpty() else ""
}