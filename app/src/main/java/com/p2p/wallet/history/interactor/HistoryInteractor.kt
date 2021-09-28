package com.p2p.wallet.history.interactor

import com.p2p.wallet.history.model.PriceHistory
import com.p2p.wallet.history.model.TransactionConverter
import com.p2p.wallet.history.model.TransactionType
import com.p2p.wallet.history.repository.TokenRepository
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionTypeParser
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.solanaj.programs.TokenProgram

class HistoryInteractor(
    private val tokenRepository: TokenRepository,
    private val rpcRepository: RpcRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun getDailyPriceHistory(sourceToken: String, destination: String, days: Int): List<PriceHistory> =
        tokenRepository.getDailyPriceHistory(sourceToken, destination, days)

    suspend fun getHourlyPriceHistory(sourceToken: String, destination: String, hours: Int): List<PriceHistory> =
        tokenRepository.getHourlyPriceHistory(sourceToken, destination, hours)

    suspend fun getHistory(publicKey: String, before: String?, limit: Int): List<TransactionType> {
        val signatures = rpcRepository.getConfirmedSignaturesForAddress(
            publicKey.toPublicKey(), before, limit
        ).map { it.signature }

        return rpcRepository.getConfirmedTransactions(signatures)
            .mapNotNull { response ->
                val data = TransactionTypeParser.parse(response)

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
                if (unknown != null) {
                    return@mapNotNull TransactionConverter.fromNetwork(unknown as UnknownDetails)
                }

                return@mapNotNull null
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

        val mint = if (transfer.isSimpleTransfer) Token.WRAPPED_SOL_MINT else transfer.mint
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