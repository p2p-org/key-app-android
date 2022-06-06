// package org.p2p.wallet.history.interactor.mapper
//
// import org.p2p.solanaj.kits.transaction.TransactionDetails
// import org.p2p.solanaj.kits.transaction.TransactionDetailsType
// import org.p2p.solanaj.kits.transaction.TransferDetails
// import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
// import org.p2p.solanaj.kits.transaction.network.meta.InstructionInfoDetailsResponse
// import org.p2p.solanaj.programs.SystemProgram
// import org.p2p.solanaj.programs.TokenProgram
// import org.p2p.wallet.user.interactor.UserInteractor
//
// object SolanaInstructionParser {
//
//    fun parse(
//        signature: String,
//        transactionRoot: ConfirmedTransactionRootResponse,
//        userInteractor: UserInteractor,
//        userPublicKey: String
//    ): List<TransactionDetails> {
//        val transactionInstructions = transactionRoot.transaction?.message?.instructions ?: emptyList()
//        val items = transactionInstructions.filter { it.parsed != null }.map { instruction ->
//            val parsedInfo = instruction.parsed
//            val type = parsedInfo?.type
//
//            when (TransactionDetailsType.valueOf(type)) {
//                TransactionDetailsType.TRANSFER, TransactionDetailsType.TRANSFER_CHECKED -> {
//                    parseTransferTransaction(
//                        signature = signature,
//                        transactionRoot = transactionRoot,
//                        parsedInfo = parsedInfo!!,
//                        userPublicKey = userPublicKey,
//                        userInteractor = userInteractor
//                    )
//                }
//
//
//            }
//        }
//        return items.filterNotNull()
//    }
//
//    private fun parseTransferTransaction(
//        signature: String,
//        transactionRoot: ConfirmedTransactionRootResponse,
//        parsedInfo: InstructionInfoDetailsResponse,
//        userPublicKey: String,
//        userInteractor: UserInteractor
//    ): TransferDetails {
//        val instructions = transactionRoot.transaction?.message?.instructions
//        val instruction  = instructions?.lastOrNull()
//        if(instruction?.programId == SystemProgram.PROGRAM_ID.toBase58()) {
//
//        }
//        val sourcePubKey = parsedInfo.info.source
//        val destinationPubKey = parsedInfo.info.destination
//        val authority = parsedInfo.info.authority
//        val instructionInfo = parsedInfo.info
//        val lamports: String = instructionInfo.lamports?.toLong()?.toBigInteger()
//            ?.toString() ?: instructionInfo.amount ?: instructionInfo.tokenAmount?.amount ?: "0"
//        val mint = parsedInfo.info.mint
//        val decimals = instructionInfo.tokenAmount?.decimals?.toInt() ?: 0
//
//        if (instruction?.programId == SystemProgram.PROGRAM_ID.toBase58()) {
//
//            return TransferDetails(
//                signature = signature,
//                blockTime = transactionRoot.blockTime,
//                slot = transactionRoot.slot,
//                fee = transactionRoot.meta.fee,
//                source = sourcePubKey,
//                destination = destinationPubKey,
//                authority = authority,
//                mint = mint,
//                amount = lamports,
//                _decimals = decimals,
//                programId = instruction.programId.orEmpty(),
//                typeStr = parsedInfo.type
//            )
//        } else {
//            var destinationAuthority: String? = null
//
//            val instructions = transactionRoot.transaction?.message?.instructions
//
//            instructions?.firstOrNull { it.programId == TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID.toBase58() }?.let {
//                destinationAuthority = it.parsed?.info?.wallet
//            }
//
//            if (destinationAuthority == null) {
//                instructions?.firstOrNull {
//                    it.programId == SystemProgram.PROGRAM_ID.toBase58() && it.parsed?.type == "initializeAccount"
//                }?.let { destinationAuthority = it.parsed?.info?.owner }
//            }
//
//            val tokenBalance = transactionRoot.meta.postTokenBalances?.firstOrNull { !it.mint.isNullOrEmpty() }
//            if (tokenBalance != null) {
//                var myAccount = userPublicKey
//                val accountKeys = transactionRoot.transaction?.message?.accountKeys.orEmpty()
//                if ((sourcePubKey != userPublicKey && destinationPubKey != myAccount) && accountKeys.size >= 4) {
//
//                    if (myAccount == accountKeys[0].publicKey) {
//                        myAccount = sourcePubKey.orEmpty()
//                    }
//                    if (myAccount == accountKeys[3].publicKey) {
//                        myAccount = destinationPubKey.orEmpty()
//                    }
//
//                    val tokenMint = userInteractor.findTokenData(tokenBalance.mint.orEmpty())?.mintAddress
//
//                    return TransferDetails(
//                        signature = signature,
//                        blockTime = transactionRoot.blockTime,
//                        slot = transactionRoot.slot,
//                        fee = transactionRoot.meta.fee,
//                        source = sourcePubKey,
//                        destination = destinationPubKey,
//                        authority = authority,
//                        mint = tokenMint,
//                        amount = lamports,
//                        _decimals = decimals,
//                        programId = instruction?.programId.orEmpty(),
//                        typeStr = parsedInfo.type
//                    )
//                }
//            } else {
//                return TransferDetails(
//                    signature = signature,
//                    blockTime = transactionRoot.blockTime,
//                    slot = transactionRoot.slot,
//                    fee = transactionRoot.meta.fee,
//                    source = sourcePubKey,
//                    destination = destinationPubKey,
//                    authority = authority,
//                    mint = tokenBalance,
//                    amount = lamports,
//                    _decimals = decimals,
//                    programId = instruction?.programId.orEmpty(),
//                    typeStr = parsedInfo.type
//                )
//            }
//        }
//        return TransferDetails(
//            signature = signature,
//            blockTime = transactionRoot.blockTime,
//            slot = transactionRoot.slot,
//            fee = transactionRoot.meta.fee,
//            source = sourcePubKey,
//            destination = destinationPubKey,
//            authority = authority,
//            mint = mint,
//            amount = lamports,
//            _decimals = decimals,
//            programId = instruction?.programId.orEmpty(),
//            typeStr = parsedInfo.type
//        )
//    }
// }
