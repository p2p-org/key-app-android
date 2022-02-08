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
import org.p2p.solanaj.programs.TokenSwapProgram
import org.p2p.wallet.feerelayer.model.RelayInfo
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.utils.Constants
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
        owner: Account,
        needsCreateUserRelayAddress: Boolean,
        sourceToken: TokenInfo,
        amount: BigInteger,
        topUpPools: OrcaPoolsPair,
        topUpFee: BigInteger
    ): List<String> {
        val blockhash = rpcRepository.getRecentBlockhash()
        val info = feeRelayerAccountInteractor.getRelayInfo()
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()

        // STEP 3: prepare for topUp
        val topUpTransaction = feeRelayerInstructionsInteractor.prepareForTopUp(
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
        val signatures = getSignatures(
            transaction = topUpTransaction.transaction,
            owner = owner,
            transferAuthorityAccount = topUpTransaction.transferAuthorityAccount
        )

        return feeRelayerRepository.relayTopUpSwap(
            userSourceTokenAccountPubkey = sourceToken.address,
            sourceTokenMintPubkey = sourceToken.mint,
            userAuthorityPubkey = owner.publicKey.toBase58(),
            swapData = topUpTransaction.swapData,
            feeAmount = topUpFee,
            signatures = signatures,
            blockhash = blockhash.recentBlockhash
        )
    }

    fun calculateSwappingFee(
        programId: PublicKey,
        info: RelayInfo,
        sourceToken: TokenInfo,
        destinationToken: TokenInfo,
        userDestinationAccountOwnerAddress: String?,
        pools: OrcaPoolsPair,
        needsCreateDestinationTokenAccount: Boolean
    ): FeeAmount {
        return prepareSwapTransaction(
            programId = programId,
            sourceToken = sourceToken,
            destinationToken = destinationToken,
            userDestinationAccountOwnerAddress = userDestinationAccountOwnerAddress,
            pools = pools,
            inputAmount = BigInteger.valueOf(10000L), // fake
            slippage = 0.05, // fake
            feeAmount = BigInteger.ZERO, // fake
            blockhash = "FR1GgH83nmcEdoNXyztnpUL2G13KkUv6iwJPwVfnqEgW", // fake
            minimumTokenAccountBalance = info.minimumTokenAccountBalance,
            needsCreateDestinationTokenAccount = needsCreateDestinationTokenAccount,
            feePayerAddress = info.feePayerAddress,
            lamportsPerSignature = info.lamportsPerSignature
        ).expectedFee
    }

    fun prepareSwapTransaction(
        programId: PublicKey,
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
        if (sourceToken.mint == Constants.WRAPPED_SOL_MINT) {
            sourceWSOLNewAccount = Account()
            val createAccountInstruction = SystemProgram.createAccount(
                userAuthorityAddress,
                sourceWSOLNewAccount.publicKey,
                (inputAmount + minimumTokenAccountBalance).toLong()
            )

            instructions += createAccountInstruction

            val initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                sourceWSOLNewAccount.publicKey,
                Constants.WRAPPED_SOL_MINT.toPublicKey(),
                userAuthorityAddress
            )
            instructions += initializeAccountInstruction

            accountCreationFee += minimumTokenAccountBalance
            userSourceTokenAccountAddress = sourceWSOLNewAccount.publicKey
        }

        // check destination
        var userDestinationTokenAccountAddress = destinationToken.address
        if (needsCreateDestinationTokenAccount) {
            val associatedAccount = TokenTransaction.getAssociatedTokenAddress(
                mint = destinationTokenMintAddress,
                owner = destinationToken.address.toPublicKey()
            )

            val createAccountInstruction = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                destinationTokenMintAddress,
                associatedAccount,
                PublicKey(destinationToken.address),
                feePayerAddress
            )
            instructions += createAccountInstruction

            accountCreationFee += minimumTokenAccountBalance
            userDestinationTokenAccountAddress = associatedAccount.toBase58()
        }

        // swap
        val transitTokenMintPubkey = feeRelayerInstructionsInteractor.getTransitTokenMintPubkey(pools)
        val (swapData, transferAuthorityAccount) = feeRelayerInstructionsInteractor.prepareSwapData(
            pools = pools,
            inputAmount = inputAmount,
            minAmountOut = null,
            slippage = slippage,
            transitTokenMintPubkey = transitTokenMintPubkey
        )
        val userTransferAuthority = transferAuthorityAccount?.publicKey

        when (swapData) {
            is SwapData.Direct -> {
                val pool = pools.firstOrNull() ?: throw IllegalStateException("Swap pools not found")
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

                // swap
                val swapInstruction = TokenSwapProgram.swapInstruction(
                    pool.account,
                    pool.authority,
                    userTransferAuthority ?: userAuthorityAddress,
                    userSourceTokenAccountAddress,
                    pool.tokenAccountA,
                    pool.tokenAccountB,
                    userDestinationTokenAccountAddress.toPublicKey(),
                    pool.poolTokenMint,
                    pool.feeAccount,
                    pool.hostFeeAccount,
                    TokenProgram.PROGRAM_ID,
                    pool.swapProgramId,
                    swapData.amountIn,
                    swapData.minimumAmountOut
                )
                instructions += swapInstruction
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
                    programId = programId,
                    feePayer = feePayerAddress,
                    userAuthority = userAuthorityAddress,
                    transitTokenAccount = transitTokenAccountAddress,
                    transitTokenMint = transitTokenMint
                )
                instructions += transitTokenAccountInstruction

                // relay swap
                val relaySwapInstruction = FeeRelayerProgram.createRelaySwapInstruction(
                    programId = programId,
                    transitiveSwap = swapData,
                    userAuthorityAddressPubkey = userAuthorityAddress,
                    sourceAddressPubkey = userSourceTokenAccountAddress,
                    transitTokenAccount = transitTokenAccountAddress,
                    destinationAddressPubkey = PublicKey(userDestinationTokenAccountAddress),
                    feePayerPubkey = feePayerAddress
                )

                instructions += relaySwapInstruction

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

            accountCreationFee -= minimumTokenAccountBalance
        }

        // close destination
        if (destinationTokenMintAddress.toBase58() == Constants.WRAPPED_SOL_MINT &&
            userDestinationAccountOwnerAddress != null
        ) {
            val ownerAddress = userDestinationAccountOwnerAddress.toPublicKey()
            val closeAccountInstruction = TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                userDestinationTokenAccountAddress.toPublicKey(),
                ownerAddress,
                ownerAddress
            )
            instructions += closeAccountInstruction

            val transferInstruction = SystemProgram.transfer(
                ownerAddress,
                feePayerAddress,
                minimumTokenAccountBalance
            )

            instructions += transferInstruction
            accountCreationFee -= minimumTokenAccountBalance
        }

        // Relay fee
        val transferSolInstruction = FeeRelayerProgram.createRelayTransferSolInstruction(
            programId = programId,
            userAuthority = userAuthorityAddress,
            recipient = feePayerAddress,
            amount = feeAmount,
            userRelayAccount = feeRelayerAccountInteractor.getUserRelayAddress(userAuthorityAddress)
        )
        instructions += transferSolInstruction

        val transaction = Transaction()
        transaction.addInstructions(instructions)
        transaction.recentBlockHash = blockhash
        transaction.setFeePayer(feePayerAddress)

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

    /*
    * Gets signature from transaction
    * */
    private fun getSignatures(
        transaction: Transaction,
        owner: Account,
        transferAuthorityAccount: Account?
    ): SwapTransactionSignatures {

        val signers = mutableListOf(owner)

        if (transferAuthorityAccount != null) {
            // fixme: this may cause presigner error
            signers.add(0, transferAuthorityAccount)
        }

        transaction.sign(signers)

        val ownerSignatureData = transaction.findSignature(owner.publicKey)?.signature

        val transferAuthoritySignatureData = if (transferAuthorityAccount != null) {
            transaction.findSignature(transferAuthorityAccount.publicKey)?.signature
        } else {
            null
        }

        if (ownerSignatureData.isNullOrEmpty()) {
            throw IllegalStateException("Invalid owner signature")
        }

        return SwapTransactionSignatures(
            userAuthoritySignature = ownerSignatureData,
            transferAuthoritySignature = transferAuthoritySignatureData
        )
    }
}