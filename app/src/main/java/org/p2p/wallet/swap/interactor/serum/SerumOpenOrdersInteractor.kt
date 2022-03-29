package org.p2p.wallet.swap.interactor.serum

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.AccountInstructions
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.programs.SerumSwapProgram
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.serumswap.OpenOrders
import org.p2p.solanaj.serumswap.OpenOrdersLayoutParser
import org.p2p.solanaj.serumswap.Version
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import java.math.BigInteger

class SerumOpenOrdersInteractor(
    private val rpcAmountRepository: RpcAmountRepository,
    private val rpcAccountRepository: RpcAccountRepository
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
            rpcAmountRepository.getMinBalanceForRentExemption(span.toInt()).toLong()
        }

        val order = Account()

        val instructions = mutableListOf(
            SystemProgram.createAccount(
                fromPublicKey = ownerAddress,
                newAccountPublicKey = order.publicKey,
                lamports = requestMinRentExemption,
                space = OpenOrders.getLayoutSpan(programId.toBase58()),
                programId = programId
            )
        )

        if (shouldInitAccount) {
            val initInstruction = SerumSwapProgram.initOrderInstruction(
                order = order.publicKey,
                marketAddress = marketAddress,
                owner = ownerAddress
            )

            instructions.add(initInstruction)
        }

        val cleanupInstructions = mutableListOf<TransactionInstruction>()
        if (closeAfterward) {
            val closeOrderInstruction = SerumSwapProgram.closeOrderInstruction(
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

    suspend fun getMinimumBalanceForRentExemption(programId: PublicKey): BigInteger {
        val span = OpenOrders.getLayoutSpan(programId.toBase58())
        return rpcAmountRepository.getMinBalanceForRentExemption(span.toInt())
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
            encoding = Encoding.BASE64.encoding,
            filters = updatedFilters
        )
        val programAccounts = rpcAccountRepository.getProgramAccounts(programId, config)

        return programAccounts.map {
            if (it.account.owner != programId.toBase58()) {
                throw IllegalStateException("The address is not owned by the program")
            }

            val decodedData = it.account.getDecodedData()
            val data = if (version == 1) {
                OpenOrdersLayoutParser.parseV1(decodedData)
            } else {
                OpenOrdersLayoutParser.parseV2(decodedData)
            }
            OpenOrders(
                address = it.pubkey,
                data = data,
                programId = programId
            )
        }
    }
}
