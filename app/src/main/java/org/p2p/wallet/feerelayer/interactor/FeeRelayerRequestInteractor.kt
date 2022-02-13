package org.p2p.wallet.feerelayer.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Signature
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.feerelayer.model.RelayInfo
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerRequestInteractor(
    private val rpcRepository: RpcRepository,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val feeRelayerInstructionsInteractor: FeeRelayerInstructionsInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    // Submits a signed top up swap transaction to the backend for processing
    suspend fun topUp(
        feeRelayerProgramId: PublicKey,
        owner: Account,
        needsCreateUserRelayAddress: Boolean,
        sourceToken: TokenInfo,
        amount: BigInteger,
        topUpPools: OrcaPoolsPair,
        topUpFee: FeeAmount
    ): List<String> {
        val blockhash = rpcRepository.getRecentBlockhash()
        val info = feeRelayerAccountInteractor.getRelayInfo()
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()

        // STEP 3: prepare for topUp
        val (swapData, topUpTransaction) = feeRelayerInstructionsInteractor.prepareForTopUp(
            feeRelayerProgramId = feeRelayerProgramId,
            sourceToken = sourceToken,
            userAuthorityAddress = owner.publicKey,
            userRelayAddress = relayAccount.publicKey,
            topUpPools = topUpPools,
            amount = amount,
            feeAmount = topUpFee,
            blockhash = blockhash.recentBlockhash,
            minimumRelayAccountBalance = info.minimumRelayAccountBalance,
            minimumTokenAccountBalance = info.minimumTokenAccountBalance,
            needsCreateUserRelayAccount = needsCreateUserRelayAddress,
            feePayerAddress = info.feePayerAddress.toBase58(),
            lamportsPerSignature = info.lamportsPerSignature
        )

        // STEP 4: send transaction
        val signatures = topUpTransaction.transaction.allSignatures
        if (signatures.size < 2) {
            throw IllegalStateException("Invalid signature")
        }

        // the second signature is the owner's signature
        val ownerSignature = signatures[1].signature

        // the third signature (optional) is the transferAuthority's signature
        val transferAuthoritySignature = signatures.getOrNull(2)?.signature

        val topUpSignatures = SwapTransactionSignatures(
            userAuthoritySignature = ownerSignature,
            transferAuthoritySignature = transferAuthoritySignature
        )

        return feeRelayerRepository.relayTopUpSwap(
            userSourceTokenAccountPubkey = sourceToken.address,
            sourceTokenMintPubkey = sourceToken.mint,
            userAuthorityPubkey = owner.publicKey.toBase58(),
            swapData = swapData,
            feeAmount = topUpFee.accountBalances,
            signatures = topUpSignatures,
            blockhash = blockhash.recentBlockhash
        )
    }

    fun calculateSwappingFee(
        info: RelayInfo,
        sourceToken: TokenInfo,
        destinationToken: TokenInfo,
        needsCreateDestinationTokenAccount: Boolean
    ): FeeAmount {
        val expectedFee = FeeAmount()

        // fee for payer's signature
        expectedFee.transaction += info.lamportsPerSignature

        // fee for owner's signature
        expectedFee.transaction += info.lamportsPerSignature

        // when source token is native SOL
        if (sourceToken.mint == WRAPPED_SOL_MINT) {
            // WSOL's signature
            expectedFee.transaction += info.lamportsPerSignature

            // TODO: - Account creation fee?
            expectedFee.accountBalances += info.minimumTokenAccountBalance
        }

        // when needed to create destination
        if (needsCreateDestinationTokenAccount && destinationToken.mint != WRAPPED_SOL_MINT) {
            expectedFee.accountBalances += info.minimumTokenAccountBalance
        }

        // when destination is native SOL
        if (destinationToken.mint == WRAPPED_SOL_MINT) {
            expectedFee.transaction += info.lamportsPerSignature
        }

        return expectedFee
    }

    // Calculate fee for given transaction
    suspend fun calculateFee(preparedTransaction: PreparedTransaction): FeeAmount {
        val fee = preparedTransaction.expectedFee
        // TODO: - Check if free transaction available
        val userRelayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val userRelayInfo = feeRelayerAccountInteractor.getRelayInfo()
        if (!userRelayAccount.isCreated) {
            fee.transaction += userRelayInfo.lamportsPerSignature // TODO: - accountBalances or transaction?
        }
        return fee
    }

    fun prepareSwapTransaction(
        feeRelayerProgramId: PublicKey,
        sourceToken: TokenInfo,
        destinationToken: TokenInfo,
        userDestinationAccountOwnerAddress: String?,

        pools: OrcaPoolsPair,
        inputAmount: BigInteger,
        slippage: Double,

        feeAmount: BigInteger,
        blockhash: String,
        minimumTokenAccountBalance: BigInteger,
        needsCreateDestinationTokenAccount: Boolean,
        feePayerAddress: PublicKey,
        lamportsPerSignature: BigInteger
    ): PreparedTransaction {
        val owner = Account(tokenKeyProvider.secretKey)

        val userAuthorityAddress = owner.publicKey

        var userSourceTokenAccountAddress = sourceToken.address.toPublicKey()
        val sourceTokenMintAddress = sourceToken.mint.toPublicKey()
        val associatedTokenAddress = TokenTransaction.getAssociatedTokenAddress(
            mint = sourceTokenMintAddress,
            owner = feePayerAddress
        )
        if (userSourceTokenAccountAddress == associatedTokenAddress) {
            throw IllegalStateException("Wrong address")
        }
        val destinationTokenMintAddress = destinationToken.mint.toPublicKey()

        // forming transaction and count fees
        var accountCreationFee: BigInteger = BigInteger.ZERO
        val instructions = mutableListOf<TransactionInstruction>()

        // check source
        var sourceWSOLNewAccount: Account? = null
        if (sourceToken.mint == WRAPPED_SOL_MINT) {
            sourceWSOLNewAccount = Account()
            val createAccountInstruction = SystemProgram.createAccount(
                feePayerAddress,
                sourceWSOLNewAccount.publicKey,
                (inputAmount + minimumTokenAccountBalance).toLong()
            )

            instructions += createAccountInstruction

            val initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                sourceWSOLNewAccount.publicKey,
                WRAPPED_SOL_MINT.toPublicKey(),
                userAuthorityAddress
            )
            instructions += initializeAccountInstruction

            userSourceTokenAccountAddress = sourceWSOLNewAccount.publicKey
        }

        // check destination
        var destinationNewAccount: Account? = null

        var userDestinationTokenAccountAddress = destinationToken.address
        if (needsCreateDestinationTokenAccount) {
            destinationNewAccount = Account()

            val createAccountInstruction = SystemProgram.createAccount(
                feePayerAddress,
                destinationNewAccount.publicKey,
                minimumTokenAccountBalance.toLong()
            )
            instructions += createAccountInstruction
            val initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                destinationNewAccount.publicKey,
                destinationTokenMintAddress,
                userAuthorityAddress
            )
            instructions += initializeAccountInstruction

            accountCreationFee += minimumTokenAccountBalance
            userDestinationTokenAccountAddress = destinationNewAccount.publicKey.toBase58()
        }

        // swap
        val transitTokenMintPubkey = feeRelayerInstructionsInteractor.getTransitTokenMintPubkey(pools)
        val (swapData, transferAuthorityAccount) = feeRelayerInstructionsInteractor.prepareSwapData(
            pools = pools,
            inputAmount = inputAmount,
            minAmountOut = null,
            slippage = slippage,
            transitTokenMintPubkey = transitTokenMintPubkey,
            userAuthorityAddress = userAuthorityAddress
        )
        val userTransferAuthority = transferAuthorityAccount?.publicKey

        val userTemporarilyWSOLAddress = feeRelayerAccountInteractor.getUserTemporaryWsolAccount(userAuthorityAddress)
        val userRelayAddress = feeRelayerAccountInteractor.getUserRelayAddress(userAuthorityAddress)
        when (swapData) {
            is SwapData.Direct -> {
                // approve
                if (userTransferAuthority != null) {
                    val approveInstruction = TokenProgram.approveInstruction(
                        TokenProgram.PROGRAM_ID,
                        userSourceTokenAccountAddress,
                        userTransferAuthority,
                        userAuthorityAddress,
                        swapData.amountIn
                    )
                    instructions += approveInstruction
                }

                // top up
                val topUpSwapDirectInstruction = FeeRelayerProgram.topUpSwapDirectInstruction(
                    feeRelayerProgramId = feeRelayerProgramId,
                    feePayer = feePayerAddress,
                    userAuthority = userAuthorityAddress,
                    userRelayAccount = userRelayAddress,
                    userTransferAuthority = PublicKey(swapData.transferAuthorityPubkey),
                    userSourceTokenAccount = userSourceTokenAccountAddress,
                    userTemporaryWsolAccount = userTemporarilyWSOLAddress,
                    swapProgramId = PublicKey(swapData.programId),
                    swapAccount = PublicKey(swapData.accountPubkey),
                    swapAuthority = PublicKey(swapData.authorityPubkey),
                    swapSource = PublicKey(swapData.sourcePubkey),
                    swapDestination = PublicKey(swapData.destinationPubkey),
                    poolTokenMint = PublicKey(swapData.poolTokenMintPubkey),
                    poolFeeAccount = PublicKey(swapData.poolFeeAccountPubkey),
                    amountIn = swapData.amountIn,
                    minimumAmountOut = swapData.minimumAmountOut
                )

                instructions += topUpSwapDirectInstruction
            }
            is SwapData.SplTransitive -> {
                // approve
                if (userTransferAuthority != null) {
                    val approveInstruction = TokenProgram.approveInstruction(
                        TokenProgram.PROGRAM_ID,
                        userSourceTokenAccountAddress,
                        userTransferAuthority,
                        userAuthorityAddress,
                        swapData.from.amountIn
                    )
                    instructions += approveInstruction
                }

                // create transit token account
                val transitTokenMint = swapData.transitTokenMintPubkey.toPublicKey()
                val transitTokenAccountAddress = feeRelayerAccountInteractor.getTransitTokenAccountAddress(
                    owner = userAuthorityAddress,
                    mint = transitTokenMint
                )

                val transitTokenAccountInstruction = FeeRelayerProgram.createTransitTokenAccountInstruction(
                    programId = feeRelayerProgramId,
                    feePayer = feePayerAddress,
                    userAuthority = userAuthorityAddress,
                    transitTokenAccount = transitTokenAccountAddress,
                    transitTokenMint = transitTokenMint
                )
                instructions += transitTokenAccountInstruction

                // Destination WSOL account funding
                accountCreationFee += minimumTokenAccountBalance

                // top up
                val topUpSwapInstruction = FeeRelayerProgram.topUpSwapInstruction(
                    feeRelayerProgramId = feeRelayerProgramId,
                    userRelayAddress = userRelayAddress,
                    userTemporarilyWSOLAddress = userTemporarilyWSOLAddress,
                    topUpSwap = swapData,
                    userAuthorityAddress = userAuthorityAddress,
                    userSourceTokenAccountAddress = userSourceTokenAccountAddress,
                    feePayerAddress = feePayerAddress
                )
                instructions += topUpSwapInstruction

                // close transit token account
                val closeAccountInstruction = TokenProgram.closeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    transitTokenAccountAddress,
                    feePayerAddress,
                    feePayerAddress
                )
                instructions += closeAccountInstruction
            }
        }

        // WSOL close
        // close source
        if (sourceWSOLNewAccount != null) {
            val closeAccountInstruction = TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                sourceWSOLNewAccount.publicKey,
                userAuthorityAddress,
                userAuthorityAddress
            )
            instructions += closeAccountInstruction

            val transferInstruction = SystemProgram.transfer(
                userAuthorityAddress,
                feePayerAddress,
                minimumTokenAccountBalance
            )

            instructions += transferInstruction
        }

        // close destination
        if (destinationNewAccount != null && destinationTokenMintAddress.toBase58() == WRAPPED_SOL_MINT) {
            val closeAccountInstruction = TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                destinationNewAccount.publicKey,
                userAuthorityAddress,
                userAuthorityAddress
            )

            instructions += closeAccountInstruction

            val transferInstruction = SystemProgram.transfer(
                userAuthorityAddress,
                feePayerAddress,
                minimumTokenAccountBalance
            )
            instructions += transferInstruction

            accountCreationFee -= minimumTokenAccountBalance
        }

        val transaction = Transaction()
        transaction.addInstructions(instructions)
        transaction.recentBlockHash = blockhash
        transaction.feePayer = feePayerAddress

        // calculate fee first
        val expectedFee = FeeAmount(
            transaction = transaction.calculateTransactionFee(lamportsPerSignature),
            accountBalances = accountCreationFee
        )

        // resign transaction
        val signers = mutableListOf(owner)
        if (sourceWSOLNewAccount != null) {
            signers += sourceWSOLNewAccount
        }

        if (destinationNewAccount != null) {
            signers += destinationNewAccount
        }
        transaction.sign(signers)

        return PreparedTransaction(transaction, signers, expectedFee)
    }

    // Generate transfer transaction
    suspend fun makeTransferTransaction(
        programId: PublicKey,
        owner: Account,
        sourceToken: TokenInfo,
        recipientPubkey: String,
        tokenMintAddress: String,
        feePayerAddress: String,
        lamportsPerSignatures: BigInteger,
        minimumTokenAccountBalance: BigInteger,
        inputAmount: BigInteger,
        decimals: Int
    ): Pair<Transaction, FeeAmount> {
        val (_, feeAmount) = createTransferTransaction(
            programId = programId,
            owner = owner,
            sourceToken = sourceToken,
            recipientPubkey = recipientPubkey,
            tokenMintAddress = tokenMintAddress,
            feePayerAddress = feePayerAddress,
            feeAmount = BigInteger.ZERO,
            lamportsPerSignatures = lamportsPerSignatures,
            minimumTokenAccountBalance = minimumTokenAccountBalance,
            inputAmount = inputAmount,
            decimals = decimals
        )

        return createTransferTransaction(
            programId = programId,
            owner = owner,
            sourceToken = sourceToken,
            recipientPubkey = recipientPubkey,
            tokenMintAddress = tokenMintAddress,
            feePayerAddress = feePayerAddress,
            feeAmount = feeAmount.total,
            lamportsPerSignatures = lamportsPerSignatures,
            minimumTokenAccountBalance = minimumTokenAccountBalance,
            inputAmount = inputAmount,
            decimals = decimals
        )
    }

    suspend fun relayTransaction(
        instructions: List<TransactionInstruction>,
        signatures: List<Signature>,
        pubkeys: List<AccountMeta>,
        blockHash: String
    ): List<String> =
        feeRelayerRepository.relayTransaction(instructions, signatures, pubkeys, blockHash)

    suspend fun getFeePayerPublicKey(): PublicKey =
        feeRelayerRepository.getFeePayerPublicKey()

    private suspend fun createTransferTransaction(
        programId: PublicKey,
        owner: Account,
        sourceToken: TokenInfo,
        recipientPubkey: String,
        tokenMintAddress: String,
        feePayerAddress: String,
        feeAmount: BigInteger,
        lamportsPerSignatures: BigInteger,
        minimumTokenAccountBalance: BigInteger,
        inputAmount: BigInteger,
        decimals: Int
    ): Pair<Transaction, FeeAmount> = withContext(Dispatchers.IO) {

        val accountInfo = rpcRepository.getAccountInfo(recipientPubkey)

        val value = accountInfo?.value
        val shouldCreateRecipientTokenAccount = value?.owner != TokenProgram.PROGRAM_ID.toBase58()

        // Calculate fee
        val expectedFee = FeeAmount()
        val instructions = mutableListOf<TransactionInstruction>()

        val recipientTokenAccountAddress = if (shouldCreateRecipientTokenAccount) {
            val associatedAccount = TokenTransaction.getAssociatedTokenAddress(
                mint = tokenMintAddress.toPublicKey(),
                owner = recipientPubkey.toPublicKey()
            )
            val instruction = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                tokenMintAddress.toPublicKey(),
                associatedAccount,
                recipientPubkey.toPublicKey(),
                feePayerAddress.toPublicKey()
            )
            instructions += instruction
            expectedFee.accountBalances += minimumTokenAccountBalance
            associatedAccount.toBase58()
        } else {
            recipientPubkey
        }

        val transferCheckedInstruction = TokenProgram.createTransferCheckedInstruction(
            TokenProgram.PROGRAM_ID,
            sourceToken.address.toPublicKey(),
            tokenMintAddress.toPublicKey(),
            recipientTokenAccountAddress.toPublicKey(),
            owner.publicKey,
            inputAmount,
            decimals
        )
        instructions += transferCheckedInstruction

        val transferSolInstruction = FeeRelayerProgram.createRelayTransferSolInstruction(
            programId = programId,
            userAuthority = owner.publicKey,
            userRelayAccount = feeRelayerAccountInteractor.getUserRelayAddress(owner.publicKey),
            recipient = feePayerAddress.toPublicKey(),
            amount = feeAmount
        )
        instructions += transferSolInstruction

        val transaction = Transaction()
        transaction.addInstructions(instructions)
        transaction.setFeePayer(feePayerAddress.toPublicKey())
        val blockhash = rpcRepository.getRecentBlockhash()
        transaction.recentBlockHash = blockhash.recentBlockhash

        expectedFee.transaction += transaction.calculateTransactionFee(lamportsPerSignatures)
        return@withContext transaction to expectedFee
    }
}