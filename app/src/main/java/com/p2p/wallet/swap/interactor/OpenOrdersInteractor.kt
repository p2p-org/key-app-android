package com.p2p.wallet.swap.interactor

import com.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.TransactionInstruction
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.serumswap.OpenOrders
import org.p2p.solanaj.serumswap.OpenOrdersLayout
import org.p2p.solanaj.serumswap.Version
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions
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

    suspend fun findForOwner(
        ownerAddress: PublicKey,
        programId: PublicKey
    ): List<OpenOrders> {
        val memcmp = ConfigObjects.Memcmp(45L, ownerAddress.toBase58())
        return getFilteredProgramAccounts(
            filters = listOf(ConfigObjects.Filter(memcmp)),
            programId = programId
        )
    }

    suspend fun getMinimumBalanceForRentExemption(programId: PublicKey): Long {
        val span = OpenOrders.getLayoutSpan(programId.toBase58())
        return rpcRepository.getMinimumBalanceForRentExemption(span)
    }

    private suspend fun getFilteredProgramAccounts(
        filters: List<Any>,
        programId: PublicKey
    ): List<OpenOrders> {
        val dataSize = ConfigObjects.DataSize(OpenOrders.getLayoutSpan(programId.toBase58()).toInt())

        val version = Version.getVersion(programId.toBase58())

        val updatedFilters = filters.toMutableList()
        updatedFilters.add(0, dataSize)
        val config = RequestConfiguration(
            encoding = Encoding.BASE64,
            filters = updatedFilters
        )
        val programAccounts = rpcRepository.getProgramAccounts(programId, config)

        return programAccounts.map {
            if (it.account.owner != programId.toBase58()) {
                throw IllegalStateException("The address is not owned by the program")
            }

            val data = if (version == 1) {
                OpenOrdersLayout.LayoutV1(it.account.getDecodedData())
            } else {
                OpenOrdersLayout.LayoutV2(it.account.getDecodedData())
            }
            OpenOrders(
                address = it.pubkey,
                data = data,
                programId = programId
            )
        }
    }
}