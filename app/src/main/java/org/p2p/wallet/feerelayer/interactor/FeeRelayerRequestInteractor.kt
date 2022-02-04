package org.p2p.wallet.feerelayer.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Signature
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.utils.crypto.Base58Utils
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
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

    // Submits a signed token swap transaction to the backend for processing
    suspend fun swap(
        owner: Account,
        sourceToken: TokenInfo,
        destinationToken: TokenInfo,
        userDestinationAccountOwnerAddress: String?,

        pools: OrcaPoolsPair,
        inputAmount: BigInteger,
        slippage: Double,

        feeAmount: BigInteger,
        minimumTokenAccountBalance: BigInteger,
        needsCreateDestinationTokenAccount: Boolean,
        feePayerAddress: String,
        lamportsPerSignature: BigInteger
    ): List<String> {
        val blockhash = rpcRepository.getRecentBlockhash()

        val preparedParams = feeRelayerInstructionsInteractor.prepareForSwapping(
            sourceToken = sourceToken,
            destinationToken = destinationToken,
            userDestinationAccountOwnerAddress = userDestinationAccountOwnerAddress,
            pools = pools,
            inputAmount = inputAmount,
            slippage = slippage,
            feeAmount = feeAmount,
            blockhash = blockhash.recentBlockhash,
            minimumTokenAccountBalance = minimumTokenAccountBalance,
            needsCreateDestinationTokenAccount = needsCreateDestinationTokenAccount,
            feePayerAddress = feePayerAddress,
            lamportsPerSignature = lamportsPerSignature
        )

        val signatures = getSignatures(
            transaction = preparedParams.transaction,
            owner = owner,
            transferAuthorityAccount = preparedParams.transferAuthorityAccount
        )

        return feeRelayerRepository.relaySwap(
            userSourceTokenAccountPubkey = sourceToken.address,
            userDestinationPubkey = destinationToken.address,
            userDestinationAccountOwner = userDestinationAccountOwnerAddress,
            sourceTokenMintPubkey = sourceToken.mint,
            destinationTokenMintPubkey = destinationToken.mint,
            userAuthorityPubkey = owner.publicKey.toBase58(),
            userSwap = preparedParams.swapData,
            feeAmount = feeAmount,
            signatures = signatures,
            blockhash = blockhash.recentBlockhash
        )
    }

    // Submits a signed transfer token transaction to the backend for processing
    suspend fun transfer(
        feeRelayerProgramId: PublicKey,
        owner: Account,
        sourceToken: TokenInfo,
        recipientPubkey: String,
        tokenMintAddress: String,
        feePayerAddress: String,
        minimumTokenAccountBalance: BigInteger,
        inputAmount: BigInteger,
        decimals: Int,
        lamportsPerSignature: BigInteger
    ): List<String> {
        val (transaction, feeAmount) = makeTransferTransaction(
            programId = feeRelayerProgramId,
            owner = owner,
            sourceToken = sourceToken,
            recipientPubkey = recipientPubkey,
            tokenMintAddress = tokenMintAddress,
            feePayerAddress = feePayerAddress,
            lamportsPerSignatures = lamportsPerSignature,
            minimumTokenAccountBalance = minimumTokenAccountBalance,
            inputAmount = inputAmount,
            decimals = decimals
        )

        val account = Account(tokenKeyProvider.secretKey)
        transaction.sign(listOf(account))

        val authoritySignature = transaction.findSignature(account.publicKey).signature
        val blockhash = transaction.recentBlockHash

        return feeRelayerRepository.relayTransferSplToken(
            senderTokenAccountPubkey = sourceToken.address,
            recipientPubkey = recipientPubkey,
            tokenMintPubkey = tokenMintAddress,
            authorityPubkey = account.publicKey.toBase58(),
            amount = inputAmount,
            feeAmount = feeAmount.total,
            decimals = decimals,
            authoritySignature = Base58Utils.encodeFromString(authoritySignature),
            blockhash = blockhash
        )
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

    suspend fun relayTransferSplToken(
        senderTokenAccountPubkey: String,
        recipientPubkey: String,
        tokenMintPubkey: String,
        authorityPubkey: String,
        amount: BigInteger,
        decimals: Int,
        feeAmount: BigInteger,
        authoritySignature: String,
        blockhash: String,
    ): List<String> =
        feeRelayerRepository.relayTransferSplToken(
            senderTokenAccountPubkey,
            recipientPubkey,
            tokenMintPubkey,
            authorityPubkey,
            amount,
            decimals,
            feeAmount,
            authoritySignature,
            blockhash
        )

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
        transferAuthorityAccount: Account
    ): SwapTransactionSignatures {

        val signers = listOf(transferAuthorityAccount, owner)
        transaction.sign(signers)

        val ownerSignatureData = transaction.findSignature(owner.publicKey)?.signature
        val transferAuthoritySignatureData = transaction.findSignature(transferAuthorityAccount.publicKey)?.signature

        if (ownerSignatureData.isNullOrEmpty() || transferAuthoritySignatureData.isNullOrEmpty()) {
            throw IllegalStateException("Invalid signatures")
        }

        return SwapTransactionSignatures(
            userAuthoritySignature = ownerSignatureData,
            transferAuthoritySignature = transferAuthoritySignatureData
        )
    }
}