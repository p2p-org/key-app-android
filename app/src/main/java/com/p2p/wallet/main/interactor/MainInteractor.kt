package com.p2p.wallet.main.interactor

import com.p2p.wallet.R
import com.p2p.wallet.amount.toLamports
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.main.repository.MainRepository
import com.p2p.wallet.rpc.RpcRepository
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.TransactionTypeParser
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.model.core.PublicKey
import java.math.BigDecimal

class MainInteractor(
    private val mainRepository: MainRepository,
    private val rpcRepository: RpcRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun sendToken(
        target: String,
        amount: BigDecimal,
        usdAmount: BigDecimal,
        decimals: Int,
        tokenSymbol: String
    ): TransactionResult {
        val currentUser = tokenKeyProvider.publicKey

        if (currentUser == target) {
            return TransactionResult.Error(R.string.main_send_to_yourself_error)
        }

        if (target.length < PublicKey.PUBLIC_KEY_LENGTH) {
            return TransactionResult.WrongWallet
        }

        val lamports = amount.toLamports(decimals)
        val recentBlockhash = rpcRepository.getRecentBlockhash()
        val signature = mainRepository.sendToken(recentBlockhash, target, lamports.toLong(), tokenSymbol)
        return TransactionResult.Success(signature, amount, usdAmount, tokenSymbol)
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
        userLocalRepository.getTokenData(mint)?.symbol.orEmpty()
}