package com.p2p.wallet.main.interactor

import com.p2p.wallet.R
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.rpc.RpcRepository
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.TransactionTypeParser
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.TransactionRequest
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import timber.log.Timber
import java.math.BigInteger

class MainInteractor(
    private val rpcRepository: RpcRepository,
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
            val instruction = TokenProgram.transferInstruction(
                tokenProgramId = TokenProgram.PROGRAM_ID,
                source = token.publicKey.toPublicKey(),
                destination = address,
                owner = payer,
                amount = lamports
            )

            transaction.addInstruction(instruction)
        }

        val recentBlockHash = rpcRepository.getRecentBlockhash()
        transaction.setRecentBlockHash(recentBlockHash.recentBlockhash)

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)

        val signature = rpcRepository.sendTransaction(transaction)
        return TransactionResult.Success(signature)
    }

    suspend fun getHistory(publicKey: String, tokenSymbol: String, limit: Int): List<Transaction> {
        val signatures = rpcRepository.getConfirmedSignaturesForAddress2(publicKey.toPublicKey(), limit)
        val rate = userLocalRepository.getPriceByToken(tokenSymbol)
        return signatures
            .mapNotNull { signature ->
                val response = rpcRepository.getConfirmedTransaction(signature.signature)
                val data = TransactionTypeParser.parse(signature.signature, response)
                val swap = data.firstOrNull { it.type == TransactionDetailsType.SWAP }
                val transfer = data.firstOrNull { it.type == TransactionDetailsType.TRANSFER }
                val close = data.firstOrNull { it.type == TransactionDetailsType.CLOSE_ACCOUNT }
                val unknown = data.firstOrNull { it.type == TransactionDetailsType.UNKNOWN }

                val (details, symbol) = when {
                    swap != null -> swap to ""
                    transfer != null -> transfer as TransferDetails to findSymbol(transfer.mint)
                    close != null -> close as CloseAccountDetails to findSymbol(close.signature)
                    else -> unknown to ""
                }

                TokenConverter.fromNetwork(details, publicKey, symbol, rate)
            }
            .sortedByDescending { it.date.toInstant().toEpochMilli() }
    }

    private fun findSymbol(mint: String): String =
        if (mint.isNotEmpty()) userLocalRepository.getTokenData(mint)?.symbol.orEmpty() else ""
}