package com.p2p.wallet.swap.interactor

import com.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.TransactionInstruction
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.serumswap.OpenOrders
import org.p2p.solanaj.serumswap.OpenOrdersLayout
import org.p2p.solanaj.serumswap.Version
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions.dexPID
import java.math.BigInteger

class OpenOrdersInteractor(
    private val rpcRepository: RpcRepository
) {

    suspend fun makeCreateAccountInstructions(
        marketAddress: PublicKey,
        ownerAddress: PublicKey,
        programId: PublicKey,
        minRentExemption: BigInteger? = null,
        shouldInitAccount: Boolean,
        closeAfterward: Boolean
    ): AccountInstructions {
        val requestMinRentExemption = if (minRentExemption != null) {
            minRentExemption.toLong()
        } else {
            val span = OpenOrders.getLayoutSpan(programId.toBase58())
            rpcRepository.getMinimumBalanceForRentExemption(span)
        }

        val order = Account()

        val instructions = mutableListOf(
            SystemProgram.createAccount(
                fromPublicKey = ownerAddress,
                newAccountPublikkey = order.publicKey,
                lamports = requestMinRentExemption,
                space = OpenOrders.getLayoutSpan(programId.toBase58()),
                programId = programId
            )
        )

        if (shouldInitAccount) {
            val initInstruction = SerumSwapInstructions.initOrderInstruction(
                order = order.publicKey,
                marketAddress = marketAddress,
                owner = ownerAddress
            )

            instructions.add(initInstruction)
        }

        val cleanupInstructions = mutableListOf<TransactionInstruction>()
        if (closeAfterward) {
            val closeOrderInstruction = SerumSwapInstructions.closeOrderInstruction(
                order = order.publicKey,
                marketAddress = marketAddress,
                owner = ownerAddress,
                destination = ownerAddress
            )

            cleanupInstructions.add(closeOrderInstruction)
        }

        return AccountInstructions(
            account = order.publicKey,
            instructions = instructions,
            cleanupInstructions = cleanupInstructions,
            signers = listOf(order)
        )
    }

    suspend fun findForMarketAndOwner(
        marketAddress: PublicKey,
        ownerAddress: PublicKey,
        programId: PublicKey = dexPID
    ): List<OpenOrders> {
        val memcmp1 = ConfigObjects.Memcmp(PublicKey.PUBLIC_KEY_LENGTH.toLong(), marketAddress.toBase58())
        val memcmp2 = ConfigObjects.Memcmp(PublicKey.PUBLIC_KEY_LENGTH.toLong(), ownerAddress.toBase58())

        val filters = listOf(
            ConfigObjects.Filter(memcmp1),
            ConfigObjects.Filter(memcmp2)
        )
        return getFilteredProgramAccounts(ownerAddress, filters, programId)
    }

    suspend fun getMinimumBalanceForRentExemption(programId: PublicKey): Long {
        val span = OpenOrders.getLayoutSpan(programId.toBase58())
        return rpcRepository.getMinimumBalanceForRentExemption(span)
    }

    private suspend fun getFilteredProgramAccounts(
        ownerAddress: PublicKey,
        filters: List<Any>,
        programId: PublicKey
    ): List<OpenOrders> {
        val updatedFilters = filters.toMutableList()
        updatedFilters.add(
            ConfigObjects.DataSize(OpenOrders.getLayoutSpan(programId.toBase58()).toInt())
        )

        val version = Version.getVersion(programId.toBase58())

        val programAccounts = rpcRepository.getProgramAccounts(
            publicKey = programId,
            filters = updatedFilters
        )

        return programAccounts.map {
            if (it.account.owner != programId.toBase58()) {
                throw IllegalStateException("The address is not owned by the program")
            }

            OpenOrders(
                address = ownerAddress,
                data = if (version == 1) {
                    OpenOrdersLayout.LayoutV1(it.account.getDecodedData())
                } else {
                    OpenOrdersLayout.LayoutV2(it.account.getDecodedData())
                },
                programId = programId
            )
        }
    }
}