package org.p2p.wallet.history.interactor.mapper

import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.CreateAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.Constants

class HistoryTransactionMapper(
    private val userLocalRepository: UserLocalRepository,
    private val historyTransactionConverter: HistoryTransactionConverter,
    private val dispatchers: CoroutineDispatchers,
) {
    suspend fun mapTransactionDetailsToHistoryTransactions(
        transactions: List<TransactionDetails>,
        accountsInfo: List<Pair<String, AccountInfo>>,
        userPublicKey: String,
        tokenPublicKey: String
    ): List<HistoryTransaction> = withContext(dispatchers.io) {
        transactions.mapNotNull { transaction ->
            when (transaction) {
                is SwapDetails -> parseOrcaSwapDetails(transaction, accountsInfo, userPublicKey)
                is BurnOrMintDetails -> parseBurnAndMintDetails(transaction, userPublicKey)
                is TransferDetails -> parseTransferDetails(transaction, tokenPublicKey, userPublicKey)
                is CloseAccountDetails -> parseCloseDetails(transaction)
                is CreateAccountDetails -> historyTransactionConverter.mapCreateAccountTransactionToHistory(transaction)
                is UnknownDetails -> historyTransactionConverter.mapUnknownTransactionToHistory(transaction)
                else -> throw IllegalStateException("Unknown transaction details $transaction")
            }
        }
            .sortedByDescending { it.date.toInstant().toEpochMilli() }
    }

    private fun parseOrcaSwapDetails(
        details: SwapDetails,
        accountsInfo: List<Pair<String, AccountInfo>>,
        userPublicKey: String
    ): HistoryTransaction? {
        val finalMintA = parseOrcaSource(details, accountsInfo) ?: return null
        val finalMintB = parseOrcaDestination(details, accountsInfo) ?: return null

        val sourceData = userLocalRepository.findTokenData(finalMintA) ?: return null
        val destinationData = userLocalRepository.findTokenData(finalMintB) ?: return null

        if (sourceData.mintAddress == destinationData.mintAddress) return null

        val destinationRate = userLocalRepository.getPriceByToken(destinationData.symbol)
        val sourceRate = userLocalRepository.getPriceByToken(sourceData.symbol)
        return historyTransactionConverter.mapSwapTransactionToHistory(
            response = details,
            sourceData = sourceData,
            destinationData = destinationData,
            sourceRate = destinationRate,
            destinationRate = sourceRate,
            sourcePublicKey = userPublicKey
        )
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
        val alternateInfo =
            TokenTransaction.parseAccountInfoData(account.second, TokenProgram.PROGRAM_ID)
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
        val alternateInfo =
            TokenTransaction.parseAccountInfoData(account.second, TokenProgram.PROGRAM_ID)
        return alternateInfo?.mint?.toBase58()
    }

    private fun parseTransferDetails(
        transfer: TransferDetails,
        directPublicKey: String,
        publicKey: String
    ): HistoryTransaction? {
        val symbol = if (transfer.isSimpleTransfer) Constants.SOL_SYMBOL else findSymbol(transfer.mint)
        val rate = userLocalRepository.getPriceByToken(symbol)

        val mint = if (transfer.isSimpleTransfer) Constants.WRAPPED_SOL_MINT else transfer.mint
        val source = userLocalRepository.findTokenData(mint.orEmpty()) ?: return null

        return historyTransactionConverter.mapTransferTransactionToHistory(
            response = transfer,
            tokenData = source,
            directPublicKey = directPublicKey,
            publicKey = publicKey,
            rate = rate
        )
    }

    private fun parseBurnAndMintDetails(details: BurnOrMintDetails, userPublicKey: String): HistoryTransaction {
        val symbol = findSymbol(details.mint)
        val rate = userLocalRepository.getPriceByToken(symbol)
        return historyTransactionConverter.mapBurnOrMintTransactionToHistory(details, userPublicKey, rate)
    }

    private fun parseCloseDetails(
        details: CloseAccountDetails
    ): HistoryTransaction {
        val symbol = findSymbol(details.mint)
        return historyTransactionConverter.mapCloseAccountTransactionToHistory(details, symbol)
    }

    private fun findSymbol(mint: String?): String {
        return if (!mint.isNullOrBlank()) {
            userLocalRepository.findTokenData(mint)?.symbol.orEmpty()
        } else {
            ""
        }
    }
}
