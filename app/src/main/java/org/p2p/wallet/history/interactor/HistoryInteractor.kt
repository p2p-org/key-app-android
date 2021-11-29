package org.p2p.wallet.history.interactor

import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.TransactionTypeParser
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.PriceHistory
import org.p2p.wallet.history.model.TransactionConverter
import org.p2p.wallet.history.repository.HistoryRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toPublicKey

class HistoryInteractor(
    private val historyRepository: HistoryRepository,
    private val rpcRepository: RpcRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun getDailyPriceHistory(sourceToken: String, destination: String, days: Int): List<PriceHistory> =
        historyRepository.getDailyPriceHistory(sourceToken, destination, days)

    suspend fun getHourlyPriceHistory(sourceToken: String, destination: String, hours: Int): List<PriceHistory> =
        historyRepository.getHourlyPriceHistory(sourceToken, destination, hours)

    suspend fun getHistory(publicKey: String, before: String?, limit: Int): List<HistoryTransaction> {
        val signatures = rpcRepository.getConfirmedSignaturesForAddress(
            publicKey.toPublicKey(), before, limit
        ).map { it.signature }

        val transactions = mutableListOf<TransactionDetails>()
        rpcRepository.getConfirmedTransactions(signatures)
            .forEach { response ->
                val data = TransactionTypeParser.parse(response)
                val swap = data.firstOrNull { it is SwapDetails }
                if (swap != null) {
                    transactions.add(swap)
                    return@forEach
                }

                val burnOrMint = data.firstOrNull { it is BurnOrMintDetails }
                if (burnOrMint != null) {
                    transactions.add(burnOrMint)
                    return@forEach
                }

                val transfer = data.firstOrNull { it is TransferDetails }
                if (transfer != null) {
                    transactions.add(transfer)
                    return@forEach
                }

                val close = data.firstOrNull { it is CloseAccountDetails }
                if (close != null) {
                    transactions.add(close)
                    return@forEach
                }

                val unknown = data.firstOrNull { it is UnknownDetails }
                if (unknown != null) {
                    transactions.add(unknown)
                    return@forEach
                }
            }

        /*
         * Making one request for all accounts info and caching values locally
         * to avoid multiple requests when constructing transaction
         * */
        val accountsInfoIds = transactions
            .flatMap { details ->
                when (details) {
                    is SwapDetails -> listOf(
                        details.source,
                        details.alternateSource,
                        details.destination,
                        details.alternateDestination
                    )
                    is CloseAccountDetails -> listOf(details.account)
                    else -> emptyList()
                }
            }
            .distinct()

        val accountsInfo = rpcRepository.getAccountsInfo(accountsInfoIds)

        return transactions
            .mapNotNull { details ->
                when (details) {
                    is SwapDetails -> parseOrcaSwapDetails(details, accountsInfo)
                    is BurnOrMintDetails -> parseBurnAndMintDetails(details)
                    is TransferDetails -> parseTransferDetails(details, publicKey, tokenKeyProvider.publicKey)
                    is CloseAccountDetails -> parseCloseDetails(details, accountsInfo)
                    is UnknownDetails -> TransactionConverter.fromNetwork(details)
                    else -> throw IllegalStateException("Unknown transaction details $details")
                }
            }
            .sortedByDescending { it.date.toInstant().toEpochMilli() }
    }

    private fun parseOrcaSwapDetails(
        details: SwapDetails,
        accountsInfo: List<Pair<String, AccountInfo>>
    ): HistoryTransaction? {
        val finalMintA = parseOrcaSource(details, accountsInfo) ?: return null
        val finalMintB = parseOrcaDestination(details, accountsInfo) ?: return null

        val sourceData = userLocalRepository.findTokenDataBySymbol(finalMintA) ?: return null
        val destinationData = userLocalRepository.findTokenDataBySymbol(finalMintB) ?: return null

        if (sourceData.mintAddress == destinationData.mintAddress) return null

        val destinationRate = userLocalRepository.getPriceByToken(destinationData.symbol)
        val source = tokenKeyProvider.publicKey
        return TransactionConverter.fromNetwork(details, sourceData, destinationData, destinationRate, source)
    }

    private fun parseOrcaSource(
        details: SwapDetails,
        accountsInfo: List<Pair<String, AccountInfo>>
    ): String? {
        if (!details.mintA.isNullOrEmpty()) {
            return details.mintA
        }

        val accountInfo = accountsInfo.find { it.first == details.source }?.second ?: return null
        val info = TokenTransaction.parseAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
        if (info != null) return info.mint.toBase58()

        val account = accountsInfo.find { it.first == details.alternateSource } ?: return null
        val alternateInfo = TokenTransaction.parseAccountInfoData(account.second, TokenProgram.PROGRAM_ID)
        return alternateInfo?.mint?.toBase58()
    }

    private fun parseOrcaDestination(
        details: SwapDetails,
        accountsInfo: List<Pair<String, AccountInfo>>
    ): String? {
        if (!details.mintB.isNullOrEmpty()) {
            return details.mintB
        }

        val accountInfo = accountsInfo.find { it.first == details.destination }?.second ?: return null
        val info = TokenTransaction.parseAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
        if (info != null) return info.mint.toBase58()

        val account = accountsInfo.find { it.first == details.alternateDestination } ?: return null
        val alternateInfo = TokenTransaction.parseAccountInfoData(account.second, TokenProgram.PROGRAM_ID)
        return alternateInfo?.mint?.toBase58()
    }

    private fun parseTransferDetails(
        transfer: TransferDetails,
        directPublicKey: String,
        publicKey: String
    ): HistoryTransaction {
        val symbol = if (transfer.isSimpleTransfer) Token.SOL_SYMBOL else findSymbol(transfer.mint)
        val rate = userLocalRepository.getPriceByToken(symbol)

        val mint = if (transfer.isSimpleTransfer) Token.WRAPPED_SOL_MINT else transfer.mint
        val source = userLocalRepository.findTokenDataBySymbol(mint)!!

        return TransactionConverter.fromNetwork(transfer, source, directPublicKey, publicKey, rate)
    }

    private fun parseBurnAndMintDetails(details: BurnOrMintDetails): HistoryTransaction {
        val symbol = findSymbol(details.mint)
        val rate = userLocalRepository.getPriceByToken(symbol)
        return TransactionConverter.fromNetwork(details, rate)
    }

    private fun parseCloseDetails(
        details: CloseAccountDetails,
        accountsInfo: List<Pair<String, AccountInfo>>
    ): HistoryTransaction? {
        val accountInfo = accountsInfo.find { it.first == details.account }?.second ?: return null
        val info = TokenTransaction.parseAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
        val symbol = findSymbol(info?.mint?.toBase58().orEmpty())
        return TransactionConverter.fromNetwork(details, symbol)
    }

    private fun findSymbol(mint: String): String =
        if (mint.isNotEmpty()) userLocalRepository.findTokenDataBySymbol(mint)?.symbol.orEmpty() else ""
}