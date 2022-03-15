package org.p2p.wallet.history.interactor

import com.google.gson.Gson
import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.ConfirmedTransactionParsed
import org.p2p.solanaj.kits.transaction.ConfirmedTransactionParser
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
import org.p2p.wallet.utils.fromJsonReified
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

class TransactionDetailsMapper(
    private val confirmedTransactionParser: ConfirmedTransactionParser
) {
    fun mapDtoToDomain(confirmedTransactions: List<ConfirmedTransactionParsed>): List<TransactionDetails> {
        val resultTransactions = mutableListOf<TransactionDetails>()

        confirmedTransactions.forEach { confirmedTransaction ->
            val parsedTransactions = confirmedTransactionParser.parseToTransactionDetails(confirmedTransaction)

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
        }

        return resultTransactions.toList()
    }

    fun mapEntityToDomain(entities: List<TransactionEntity>): List<TransactionDetails> {
        return entities.map {
            when (it) {
                is CreateAccountTransactionEntity -> {
                    CreateAccountDetails(
                        it.identifiers.signature,
                        it.identifiers.blockId,
                        it.commonInformation.blockTimeSec,
                        it.fee,
                    )
                }
                is CloseAccountTransactionEntity -> {
                    CloseAccountDetails(
                        it.identifiers.signature,
                        it.commonInformation.blockTimeSec,
                        it.identifiers.blockId,
                        it.account,
                        it.mint
                    )
                }
                is RenBtcBurnOrMintTransactionEntity -> {
                    BurnOrMintDetails(
                        it.identifiers.signature,
                        it.commonInformation.blockTimeSec,
                        it.identifiers.blockId,
                        it.account,
                        it.authority,
                        it.amount,
                        it.decimals,
                        it.fee
                    )
                }
                is SwapTransactionEntity -> {
                    SwapDetails(
                        it.identifiers.signature,
                        it.commonInformation.blockTimeSec,
                        it.identifiers.blockId,
                        it.fee,
                        it.aEntity.source.value,
                        it.bEntity.destination.value,
                        it.aEntity.amount,
                        it.bEntity.amount,
                        it.aEntity.mint,
                        it.bEntity.mint,
                        it.aEntity.alternateSource.value,
                        it.bEntity.alternateDestination.value
                    )
                }
                is TransferTransactionEntity -> {
                    TransferDetails(
                        it.identifiers.signature,
                        it.commonInformation.blockTimeSec,
                        it.identifiers.blockId,
                        it.destination.value,
                        it.source.value,
                        it.authority,
                        it.mint,
                        it.amount,
                        it.decimals,
                        it.fee
                    )
                }
                is UnknownTransactionEntity -> {
                    UnknownDetails(
                        it.identifiers.signature,
                        it.commonInformation.blockTimeSec,
                        it.identifiers.blockId,
                        Gson().fromJsonReified<Map<String, Any>>(it.rawData)
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
                        amount = it.amount,
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
                            source = it.source.toBase58Instance(),
                            alternateSource = it.alternateSource.toBase58Instance()
                        ),
                        bEntity = SwapBEntity(
                            mint = it.mintB,
                            amount = it.amountB,
                            destination = it.destination.toBase58Instance(),
                            alternateDestination = it.alternateDestination.toBase58Instance()
                        ),
                        fee = it.fee
                    )
                }
                is TransferDetails -> {
                    TransferTransactionEntity(
                        identifiers = identifiers,
                        commonInformation = commonInformation,
                        source = it.source.toBase58Instance(),
                        destination = it.destination.toBase58Instance(),
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
                        rawData = Gson().toJson(it.info),
                        data = it.data.orEmpty(),
                    )
                }
                else -> {
                    Timber.w(
                        IllegalArgumentException("Unknown type for mapping from domain: ${it.javaClass.simpleName}")
                    )
                    null
                }
            }
        }
    }

    private fun TransactionDetails.toCommonInformation(): CommonTransactionInformationEntity {
        return CommonTransactionInformationEntity(
            blockTimeSec = this.getBlockTimeInMillis() / 1000,
            transactionDetailsType = this.type?.toEntity() ?: TransactionTypeEntity.UNKNOWN,
            information = Gson().toJson(this.info)
        )
    }

    private fun TransactionDetails.toIdentifiers(): TransactionIdentifiersEntity {
        return TransactionIdentifiersEntity(
            this.signature,
            this.slot
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