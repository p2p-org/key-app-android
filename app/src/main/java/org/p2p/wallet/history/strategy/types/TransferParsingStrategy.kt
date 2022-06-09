package org.p2p.wallet.history.strategy.types

import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingStrategy
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.orZero
import org.p2p.wallet.utils.toBase58Instance

class TransferParsingStrategy(
    private val tokenKeyProvider: TokenKeyProvider,
    private val userInteractor: UserInteractor,
    private val userAccountRepository: RpcAccountRepository
) : TransactionParsingStrategy {

    override suspend fun parseTransaction(
        signature: String,
        instruction: InstructionResponse,
        transactionRoot: ConfirmedTransactionRootResponse
    ): ParsingResult {
        val instructions = transactionRoot.transaction?.message?.instructions
        val instruction = instructions?.lastOrNull()

        val parsedInfo = instruction?.parsed

        val sourcePubKey = parsedInfo?.info?.source
        val destinationPubKey = parsedInfo?.info?.destination

        val authority = parsedInfo?.info?.authority
        val instructionInfo = parsedInfo?.info

        val lamports: String = instructionInfo?.lamports?.toLong()?.toBigInteger()
            ?.toString() ?: instructionInfo?.amount ?: instructionInfo?.tokenAmount?.amount ?: "0"
        val mint = parsedInfo?.info?.mint
        val decimals = instructionInfo?.tokenAmount?.decimals?.toInt() ?: 0

        var parsingResult = TransferDetails(
            signature = signature,
            blockTime = transactionRoot.blockTime,
            slot = transactionRoot.slot,
            fee = transactionRoot.meta.fee,
            source = sourcePubKey,
            destination = destinationPubKey,
            authority = null,
            mint = mint,
            amount = lamports,
            _decimals = decimals,
            programId = instruction?.programId.orEmpty(),
            account = tokenKeyProvider.publicKey,
            typeStr = parsedInfo?.type
        )
        if (instruction?.programId != SystemProgram.PROGRAM_ID.toBase58()) {
            val postTokenBalances = transactionRoot.meta.postTokenBalances ?: emptyList()
            val accountKeys = transactionRoot.transaction?.message?.accountKeys

            var destinationAuthority: String? = null
            val createATokenInstruction =
                instructions?.firstOrNull { it.programId == TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID.toBase58() }
            val initAccountInstruction =
                instructions?.firstOrNull { it.programId == "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA" }
            if (createATokenInstruction != null) {
                destinationAuthority = createATokenInstruction.parsed?.info?.wallet.toString()
            } else if (initAccountInstruction != null && initAccountInstruction.parsed?.type == "initializeAccount") {
                destinationAuthority = initAccountInstruction.parsed?.info?.owner.toString()
            }

            // Define token with mint

            val tokenBalance = postTokenBalances.firstOrNull { !it.mint.isNullOrEmpty() }
            var myAccount: String = tokenKeyProvider.publicKey
            if (sourcePubKey != myAccount && destinationPubKey != myAccount && accountKeys?.size.orZero() >= 4) {

                if (myAccount.toBase58Instance() == accountKeys?.get(0)?.publicKey?.toBase58Instance()) {
                    myAccount = sourcePubKey.toString()
                }

                if (myAccount.toBase58Instance() == accountKeys?.get(3)?.publicKey?.toBase58Instance()) {
                    myAccount = destinationPubKey.toString()
                }
                val token = tokenBalance?.mint?.let { userInteractor.findTokenData(it) }

                var accountInfo = (sourcePubKey ?: destinationPubKey)?.let { userAccountRepository.getAccountInfo(it) }
                val mint = token?.mintAddress ?: accountInfo?.value?.mint
                parsingResult = TransferDetails(
                    signature = signature,
                    blockTime = transactionRoot.blockTime,
                    slot = transactionRoot.slot,
                    fee = transactionRoot.meta.fee,
                    source = sourcePubKey,
                    destination = destinationPubKey,
                    authority = authority,
                    mint = mint,
                    amount = lamports,
                    _decimals = decimals,
                    programId = instruction?.programId.orEmpty(),
                    typeStr = parsedInfo?.type,
                    account = myAccount
                )
            }
        }
        return ParsingResult.Transaction.create(parsingResult)
    }

    override fun getType(): TransactionDetailsType = TransactionDetailsType.TRANSFER
}
