package org.p2p.wallet.history.repository

import com.google.gson.Gson
import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.parser.ConfirmedTransactionRootParser
import org.p2p.solanaj.kits.transaction.CreateAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.wallet.history.db.entities.CloseAccountTransactionEntity
import org.p2p.wallet.history.db.entities.CreateAccountTransactionEntity
import org.p2p.wallet.history.db.entities.RenBtcBurnOrMintTransactionEntity
import org.p2p.wallet.history.db.entities.SwapAEntity
import org.p2p.wallet.history.db.entities.SwapBEntity
import org.p2p.wallet.history.db.entities.SwapTransactionEntity
import org.p2p.wallet.history.db.entities.TransactionEntity
import org.p2p.wallet.history.db.entities.TransferTransactionEntity
import org.p2p.wallet.history.db.entities.UnknownTransactionEntity
import org.p2p.wallet.history.db.entities.embedded.CommonTransactionInformationEntity
import org.p2p.wallet.history.db.entities.embedded.TransactionIdentifiersEntity
import org.p2p.wallet.history.db.entities.embedded.TransactionTypeEntity
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import java.util.concurrent.TimeUnit

class TransactionDetailsMapper(
    private val confirmedTransactionParser: ConfirmedTransactionRootParser,
    private val gson: Gson
) {
    fun mapNetworkToDomain(
        confirmedTransactionRoots: List<ConfirmedTransactionRootResponse>
    ): List<TransactionDetails> {
        val resultTransactions = mutableListOf<TransactionDetails>()

        confirmedTransactionRoots.forEach { confirmedTransaction ->
            val parsedTransactions = confirmedTransactionParser.parse(
                confirmedTransaction,
                onErrorLogger = { Timber.w(it) }
            )

            val swapTransaction = parsedTransactions.firstOrNull { it is SwapDetails }
            if (swapTransaction != null) {
                resultTransactions.add(swapTransaction)
                return@forEach
            }

            val burnOrMintTransaction = parsedTransactions.firstOrNull { it is BurnOrMintDetails }
            if (burnOrMintTransaction != null) {
                resultTransactions.add(burnOrMintTransaction)
                return@forEach
            }

            val transferTransaction = parsedTransactions.firstOrNull { it is TransferDetails }
            if (transferTransaction != null) {
                resultTransactions.add(transferTransaction)
                return@forEach
            }

            val createTransaction = parsedTransactions.firstOrNull { it is CreateAccountDetails }
            if (createTransaction != null) {
                resultTransactions.add(createTransaction)
                return@forEach
            }

            val closeTransaction = parsedTransactions.firstOrNull { it is CloseAccountDetails }
            if (closeTransaction != null) {
                resultTransactions.add(closeTransaction)
                return@forEach
            }

            val unknownTransaction = parsedTransactions.firstOrNull { it is UnknownDetails }
            if (unknownTransaction != null) {
                resultTransactions.add(unknownTransaction)
                return@forEach
            }

            val unknownTransactionTypeLogData =
                "(parsedTransactions=$parsedTransactions;\nconfirmedTransaction=${confirmedTransaction.transaction}"
            Timber.w("unknown transactions type, skipping $unknownTransactionTypeLogData")
        }

        Timber.d("Parsing finished: ${resultTransactions.size}; total=${confirmedTransactionRoots.size}")
        return resultTransactions.toList()
    }

    fun mapEntityToDomain(entities: List<TransactionEntity>): List<TransactionDetails> {
        return entities.map {
            when (it) {
                is CreateAccountTransactionEntity -> {
                    CreateAccountDetails(
                        signature = it.identifiers.signature,
                        slot = it.identifiers.blockId,
                        blockTime = it.commonInformation.blockTimeSec,
                        fee = it.fee,
                    )
                }
                is CloseAccountTransactionEntity -> {
                    CloseAccountDetails(
                        signature = it.identifiers.signature,
                        blockTime = it.commonInformation.blockTimeSec,
                        slot = it.identifiers.blockId,
                        account = it.account,
                        mint = it.mint
                    )
                }
                is RenBtcBurnOrMintTransactionEntity -> {
                    BurnOrMintDetails(
                        signature = it.identifiers.signature,
                        blockTime = it.commonInformation.blockTimeSec,
                        slot = it.identifiers.blockId,
                        fee = it.fee,
                        account = it.account,
                        authority = it.authority,
                        uiAmount = it.amount,
                        _decimals = it.decimals,
                    )
                }
                is SwapTransactionEntity -> {
                    SwapDetails(
                        signature = it.identifiers.signature,
                        blockTime = it.commonInformation.blockTimeSec,
                        slot = it.identifiers.blockId,
                        fee = it.fee,
                        source = it.aEntity.source?.value,
                        destination = it.bEntity.destination?.value,
                        amountA = it.aEntity.amount,
                        amountB = it.bEntity.amount,
                        mintA = it.aEntity.mint,
                        mintB = it.bEntity.mint,
                        alternateSource = it.aEntity.alternateSource?.value,
                        alternateDestination = it.bEntity.alternateDestination?.value
                    )
                }
                is TransferTransactionEntity -> {
                    TransferDetails(
                        signature = it.identifiers.signature,
                        blockTime = it.commonInformation.blockTimeSec,
                        slot = it.identifiers.blockId,
                        typeStr = it.commonInformation.transactionDetailsType.typeStr,
                        fee = it.fee,
                        source = it.source?.value,
                        destination = it.destination?.value,
                        authority = it.authority,
                        mint = it.mint,
                        amount = it.amount,
                        _decimals = it.decimals,
                    )
                }
                is UnknownTransactionEntity -> {
                    UnknownDetails(
                        signature = it.identifiers.signature,
                        blockTime = it.commonInformation.blockTimeSec,
                        slot = it.identifiers.blockId
                    )
                }
            }
        }
    }

    fun mapDomainToEntity(transactions: List<TransactionDetails>): List<TransactionEntity> {
        return transactions.mapNotNull {
            val commonInformation = it.toCommonInformation()
            val identifiers = it.toIdentifiers()
            when (it) {
                is CreateAccountDetails -> {
                    CreateAccountTransactionEntity(
                        identifiers = identifiers,
                        commonInformation = commonInformation,
                        fee = it.fee
                    )
                }
                is CloseAccountDetails -> {
                    CloseAccountTransactionEntity(
                        identifiers = identifiers,
                        commonInformation = commonInformation,
                        account = it.account,
                        mint = it.mint
                    )
                }
                is BurnOrMintDetails -> {
                    RenBtcBurnOrMintTransactionEntity(
                        identifiers = identifiers,
                        commonInformation = commonInformation,
                        account = it.account,
                        authority = it.authority,
                        amount = it.uiAmount,
                        decimals = it.decimals,
                        fee = it.fee
                    )
                }
                is SwapDetails -> {
                    SwapTransactionEntity(
                        identifiers = identifiers,
                        commonInformation = commonInformation,
                        aEntity = SwapAEntity(
                            mint = it.mintA,
                            amount = it.amountA,
                            source = it.source?.toBase58Instance(),
                            alternateSource = it.alternateSource?.toBase58Instance()
                        ),
                        bEntity = SwapBEntity(
                            mint = it.mintB,
                            amount = it.amountB,
                            destination = it.destination?.toBase58Instance(),
                            alternateDestination = it.alternateDestination?.toBase58Instance()
                        ),
                        fee = it.fee
                    )
                }
                is TransferDetails -> {
                    TransferTransactionEntity(
                        identifiers = identifiers,
                        commonInformation = commonInformation,
                        source = it.source?.toBase58Instance(),
                        destination = it.destination?.toBase58Instance(),
                        authority = it.authority.orEmpty(),
                        mint = it.mint,
                        amount = it.amount,
                        decimals = it.decimals,
                        fee = it.fee
                    )
                }
                is UnknownDetails -> {
                    UnknownTransactionEntity(
                        identifiers = identifiers,
                        commonInformation = commonInformation,
                        rawData = gson.toJson(it.info),
                        data = it.data.orEmpty(),
                    )
                }
                else -> {
                    Timber.e(
                        IllegalArgumentException("Unknown type for mapping from domain: ${it.javaClass.simpleName}")
                    )
                    null
                }
            }
        }
    }

    private fun TransactionDetails.toCommonInformation(): CommonTransactionInformationEntity {
        return CommonTransactionInformationEntity(
            blockTimeSec = TimeUnit.MILLISECONDS.toSeconds(this.getBlockTimeInMillis()),
            transactionDetailsType = this.type?.toEntity() ?: TransactionTypeEntity.UNKNOWN,
        )
    }

    private fun TransactionDetails.toIdentifiers(): TransactionIdentifiersEntity {
        return TransactionIdentifiersEntity(
            signature = this.signature,
            blockId = this.slot
        )
    }

    private fun TransactionDetailsType.toEntity(): TransactionTypeEntity {
        return when (this) {
            TransactionDetailsType.CREATE_ACCOUNT -> TransactionTypeEntity.CREATE_ACCOUNT
            TransactionDetailsType.UNKNOWN -> TransactionTypeEntity.UNKNOWN
            TransactionDetailsType.SWAP -> TransactionTypeEntity.SWAP
            TransactionDetailsType.TRANSFER -> TransactionTypeEntity.TRANSFER
            TransactionDetailsType.CLOSE_ACCOUNT -> TransactionTypeEntity.CLOSE_ACCOUNT
        }
    }
}