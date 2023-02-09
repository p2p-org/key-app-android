package org.p2p.wallet.history.interactor.mapper

import kotlinx.coroutines.withContext
import org.p2p.core.utils.Constants
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

class HistoryTransactionMapper(
    private val userLocalRepository: UserLocalRepository,
    private val historyTransactionConverter: HistoryTransactionConverter,
    private val dispatchers: CoroutineDispatchers,
) {
    suspend fun mapTransactionDetailsToHistoryTransactions(
        transactions: List<TransactionDetails>,
        accountsInfo: List<Pair<String, AccountInfo>>,
        userPublicKey: String
    ): List<HistoryTransaction> = withContext(dispatchers.io) {
        transactions.mapNotNull { transaction ->

            when (transaction) {
                is SwapDetails -> parseOrcaSwapDetails(transaction, accountsInfo, userPublicKey)
                is BurnOrMintDetails -> parseBurnAndMintDetails(transaction, userPublicKey)
                is TransferDetails -> parseTransferDetails(transaction, transaction.account.orEmpty(), userPublicKey)
                is CloseAccountDetails -> parseCloseDetails(transaction)
                is CreateAccountDetails -> parseCreateDetails(transaction)
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

        val destinationRate = userLocalRepository.getPriceByTokenId(destinationData.coingeckoId)
        val sourceRate = userLocalRepository.getPriceByTokenId(sourceData.coingeckoId)
        return historyTransactionConverter.mapSwapTransactionToHistory(
            response = details,
            sourceData = sourceData,
            destinationData = destinationData,
            sourceRate = sourceRate,
            destinationRate = destinationRate,
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
        val alternateInfo =
            TokenTransaction.parseAccountInfoData(account.second, TokenProgram.PROGRAM_ID)
        return alternateInfo?.mint?.toBase58()
    }

    private fun parseTransferDetails(
        transfer: TransferDetails,
        directPublicKey: String,
        publicKey: String
    ): HistoryTransaction? {
        val mint = if (transfer.isSimpleTransfer) Constants.WRAPPED_SOL_MINT else transfer.mint
        val source = mint?.let { userLocalRepository.findTokenData(it) } ?: return null
        val rate = userLocalRepository.getPriceByTokenId(source.coingeckoId)
        return historyTransactionConverter.mapTransferTransactionToHistory(
            response = transfer,
            tokenData = source,
            directPublicKey = directPublicKey,
            publicKey = publicKey,
            rate = rate
        )
    }

    private fun parseBurnAndMintDetails(details: BurnOrMintDetails, userPublicKey: String): HistoryTransaction {
        val source = details.mint.let { userLocalRepository.findTokenData(it) }
        val rate = source?.coingeckoId?.let { userLocalRepository.getPriceByTokenId(it) }
        return historyTransactionConverter.mapBurnOrMintTransactionToHistory(
            response = details,
            tokenData = source,
            userPublicKey = userPublicKey,
            rate = rate
        )
    }

    private fun parseCreateDetails(
        details: CreateAccountDetails
    ): HistoryTransaction {
        val symbol = findSymbol(details.mint)
        val source = details.mint?.let { userLocalRepository.findTokenData(it) }
        return historyTransactionConverter.mapCreateAccountTransactionToHistory(details, source, symbol)
    }

    private fun parseCloseDetails(
        details: CloseAccountDetails
    ): HistoryTransaction {
        val symbol = findSymbol(details.mint)
        val source = details.mint?.let { userLocalRepository.findTokenData(it) }
        return historyTransactionConverter.mapCloseAccountTransactionToHistory(details, source, symbol)
    }

    private fun findSymbol(mint: String?): String {
        return if (!mint.isNullOrBlank()) {
            userLocalRepository.findTokenData(mint)?.symbol ?: Constants.SOL_SYMBOL
        } else {
            Constants.SOL_SYMBOL
        }
    }
}
