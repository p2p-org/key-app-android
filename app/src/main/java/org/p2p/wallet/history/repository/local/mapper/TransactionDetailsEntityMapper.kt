package org.p2p.wallet.history.repository.local.mapper

import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.CreateAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.wallet.history.repository.local.db.entities.CloseAccountTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.CreateAccountTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.RenBtcBurnOrMintTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.SwapAEntity
import org.p2p.wallet.history.repository.local.db.entities.SwapBEntity
import org.p2p.wallet.history.repository.local.db.entities.SwapTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.TransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.TransferTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.UnknownTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.embedded.CommonTransactionInformationEntity
import org.p2p.wallet.history.repository.local.db.entities.embedded.TransactionIdentifiersEntity
import org.p2p.wallet.history.repository.local.db.entities.embedded.TransactionTypeEntity
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.crypto.toBase58Instance

class TransactionDetailsEntityMapper(private val dispatchers: CoroutineDispatchers) {
    suspend fun fromEntityToDomain(
        entities: List<TransactionEntity>
    ): List<TransactionDetails> = withContext(dispatchers.io) {
        entities.map {
            when (it) {
                is CreateAccountTransactionEntity -> {
                    CreateAccountDetails(
                        signature = it.identifiers.signature,
                        slot = it.identifiers.blockId,
                        blockTime = it.commonInformation.blockTimeSec,
                        fee = it.fee,
                        mint = it.mint
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
                        source = it.aEntity.source?.base58Value,
                        destination = it.bEntity.destination?.base58Value,
                        amountA = it.aEntity.amount,
                        amountB = it.bEntity.amount,
                        mintA = it.aEntity.mint,
                        mintB = it.bEntity.mint,
                        alternateSource = it.aEntity.alternateSource?.base58Value,
                        alternateDestination = it.bEntity.alternateDestination?.base58Value
                    )
                }
                is TransferTransactionEntity -> {
                    TransferDetails(
                        signature = it.identifiers.signature,
                        blockTime = it.commonInformation.blockTimeSec,
                        slot = it.identifiers.blockId,
                        fee = it.fee,
                        source = it.source?.base58Value,
                        destination = it.destination?.base58Value,
                        authority = it.authority,
                        mint = it.mint,
                        amount = it.amount,
                        _decimals = it.decimals,
                        programId = it.programId,
                        typeStr = it.commonInformation.transactionDetailsType.typeStr
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

    suspend fun fromDomainToEntity(
        transactions: List<TransactionDetails>
    ): List<TransactionEntity> = withContext(dispatchers.io) {
        transactions.map {
            val commonInformation = it.toCommonInformation()
            val identifiers = it.toIdentifiers()
            when (it) {
                is CreateAccountDetails -> {
                    CreateAccountTransactionEntity(
                        identifiers = identifiers,
                        commonInformation = commonInformation,
                        fee = it.fee,
                        mint = it.mint
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
                        fee = it.fee,
                        programId = it.programId
                    )
                }
                is UnknownDetails -> {
                    UnknownTransactionEntity(
                        identifiers = identifiers,
                        commonInformation = commonInformation,
                    )
                }
            }
        }
    }

    private fun TransactionDetails.toCommonInformation(): CommonTransactionInformationEntity {
        return CommonTransactionInformationEntity(
            blockTimeSec = blockTimeSeconds,
            transactionDetailsType = this.type.toEntity(),
        )
    }

    private fun TransactionDetails.toIdentifiers(): TransactionIdentifiersEntity {
        return TransactionIdentifiersEntity(
            signature = this.signature,
            blockId = this.slot
        )
    }

    private fun TransactionDetailsType.toEntity(): TransactionTypeEntity = when (this) {
        TransactionDetailsType.CREATE_ACCOUNT -> TransactionTypeEntity.CREATE_ACCOUNT
        TransactionDetailsType.SWAP -> TransactionTypeEntity.SWAP
        TransactionDetailsType.TRANSFER -> TransactionTypeEntity.TRANSFER
        TransactionDetailsType.CLOSE_ACCOUNT -> TransactionTypeEntity.CLOSE_ACCOUNT
        else -> TransactionTypeEntity.UNKNOWN
    }
}
