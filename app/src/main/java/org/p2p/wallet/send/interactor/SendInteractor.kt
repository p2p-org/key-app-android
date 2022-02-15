package org.p2p.wallet.send.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.R
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerTopUpInteractor
import org.p2p.wallet.feerelayer.model.SendStrategy
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.interactor.TransactionAmountInteractor
import org.p2p.wallet.rpc.model.FeeRelayerSendFee
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.send.model.CheckAddressResult
import org.p2p.wallet.send.model.SendResult
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getInputAmount
import org.p2p.wallet.swap.model.orca.TransactionAddressData
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigInteger

class SendInteractor(
    private val rpcRepository: RpcRepository,
    private val addressInteractor: TransactionAddressInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val orcaSwapInteractor: OrcaSwapInteractor,
    private val amountInteractor: TransactionAmountInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    companion object {
        private const val SEND_TAG = "SEND"
    }

    /*
    * If transaction will need to create a new account,
    * then the fee for account creation will be paid via this token
    * */
    private lateinit var feePayerToken: Token.Active

    /*
    * Initialize fee payer token
    * */
    suspend fun initialize(sol: Token.Active) {
        feePayerToken = sol
        feeRelayerInteractor.load()

        orcaSwapInteractor.load()
    }

    fun getFeePayerToken(): Token.Active = feePayerToken

    fun setFeePayerToken(newToken: Token.Active) {
        if (!this::feePayerToken.isInitialized) throw IllegalStateException("PayToken is not initialized")
        if (newToken.publicKey.equals(feePayerToken)) return

        feePayerToken = newToken
    }

    suspend fun getFeeTokenAccounts(fromPublicKey: String): List<Token.Active> =
        feeRelayerAccountInteractor.getFeeTokenAccounts(fromPublicKey)

    suspend fun checkAddress(destinationAddress: PublicKey, token: Token.Active): CheckAddressResult =
        try {
            val address = addressInteractor.findAssociatedAddress(destinationAddress, token.mintAddress)
            if (address.shouldCreateAssociatedInstruction) {
                CheckAddressResult.NewAccountNeeded(feePayerToken)
            } else {
                CheckAddressResult.AccountExists
            }
        } catch (e: IllegalStateException) {
            CheckAddressResult.InvalidAddress
        }

    suspend fun calculateFeesForFeeRelayer(): FeeRelayerSendFee {
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()

        var accountCreationFee = BigInteger.ZERO

        if (!relayAccount.isCreated) {
            accountCreationFee += relayInfo.minimumRelayAccountBalance
        }

        accountCreationFee += relayInfo.minimumTokenAccountBalance

        val feeInSol = accountCreationFee
        val fee = FeeRelayerSendFee(feeInSol, null)

        val poolsPairs = orcaSwapInteractor.getTradablePoolsPairs(feePayerToken.mintAddress, WRAPPED_SOL_MINT)
        if (poolsPairs.isNullOrEmpty()) return fee

        val bestPoolPair = orcaSwapInteractor.findBestPoolsPairForEstimatedAmount(feeInSol, poolsPairs)
        if (bestPoolPair.isNullOrEmpty()) return fee

        val routeValues = bestPoolPair.joinToString { "${it.tokenAName} -> ${it.tokenBName} (${it.deprecated})" }

        Timber.tag("POOLPAIR").d(routeValues)

        val feeInPayingToken = bestPoolPair.getInputAmount(feeInSol, Slippage.PERCENT.doubleValue) ?: return fee

        return fee.copy(feeInPayingToken = feeInPayingToken)
    }

    suspend fun sendTransaction(
        destinationAddress: PublicKey,
        token: Token.Active,
        lamports: BigInteger
    ): SendResult {
        val strategy = when {
            feePayerToken.isSOL && token.isSOL -> SendStrategy.SimpleSol
            feePayerToken.isSOL -> SendStrategy.Spl
            else -> SendStrategy.FeeRelayer
        }

        return when (strategy) {
            is SendStrategy.SimpleSol -> sendNativeSolToken(
                destinationAddress = destinationAddress,
                lamports = lamports
            )
            is SendStrategy.Spl -> sendSplToken(
                destinationAddress = destinationAddress,
                sourceToken = token,
                lamports = lamports
            )
            is SendStrategy.FeeRelayer -> sendByFeeRelayer(
                destinationAddress = destinationAddress,
                sourceToken = token,
                lamports = lamports
            )
        }
    }

    /**
     *  Top up and make a transaction
     *  STEP 1: Prepare all information needed for the transaction
     *  STEP 2: Calculate fee needed for transaction
     *  STEP 3: Check if relay account has already had enough balance to cover transaction fee
     *  STEP 3.1: If relay account has not been created or has not have enough balance, do top up
     *  STEP 3.1.1: Top up with needed amount
     *  STEP 4: Make transaction
     *  STEP 3.2: Else, skip top up
     *  STEP 3.2.1: Make transaction
     *
     *  @return: Array of strings contain transactions' signatures
     * */
    private suspend fun sendByFeeRelayer(
        destinationAddress: PublicKey,
        sourceToken: Token.Active,
        lamports: BigInteger
    ): SendResult {
        val currentUser = tokenKeyProvider.publicKey

        if (destinationAddress.toBase58().length < PublicKey.PUBLIC_KEY_LENGTH) {
            return SendResult.WrongWallet
        }

        val address = try {
            addressInteractor.findAssociatedAddress(destinationAddress, sourceToken.mintAddress)
        } catch (e: IllegalStateException) {
            return SendResult.WrongWallet
        }

        if (currentUser == address.associatedAddress.toBase58()) {
            return SendResult.Error(R.string.main_send_to_yourself_error)
        }

        val preparedTransaction = buildTransaction(
            address = address,
            sourceToken = sourceToken,
            destinationAddress = destinationAddress,
            lamports = lamports
        )

        val transactionId = feeRelayerInteractor.topUpAndRelayTransaction(
            preparedTransaction = preparedTransaction,
            payingFeeToken = TokenInfo(feePayerToken.publicKey, feePayerToken.mintAddress)
        ).firstOrNull().orEmpty()

        return SendResult.Success(transactionId)
    }

    private suspend fun sendNativeSolToken(
        destinationAddress: PublicKey,
        lamports: BigInteger
    ): SendResult {
        val currentUser = tokenKeyProvider.publicKey

        if (currentUser == destinationAddress.toBase58()) {
            return SendResult.Error(R.string.main_send_to_yourself_error)
        }

        val accountInfo = rpcRepository.getAccountInfo(destinationAddress.toBase58())
        val value = accountInfo?.value

        if (value?.owner == TokenProgram.PROGRAM_ID.toBase58()) {
            Timber.tag(SEND_TAG).d("Owner address matches the program id, returning with error")
            return SendResult.WrongWallet
        }

        val payer = tokenKeyProvider.publicKey.toPublicKey()

        val transaction = Transaction()
        val instructions = mutableListOf<TransactionInstruction>()

        val instruction = SystemProgram.transfer(
            fromPublicKey = payer,
            toPublicKey = destinationAddress,
            lamports = lamports
        )
        transaction.addInstruction(instruction)
        instructions += instruction

        val feePayerPublicKey = feeRelayerAccountInteractor.getRelayInfo().feePayerAddress
        val recentBlockhash = rpcRepository.getRecentBlockhash()

        transaction.feePayer = feePayerPublicKey
        transaction.recentBlockHash = recentBlockhash.recentBlockhash

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)

        val signature = feeRelayerTopUpInteractor.relayTransaction(transaction).firstOrNull().orEmpty()

        return SendResult.Success(signature)
    }

    private suspend fun sendSplToken(
        destinationAddress: PublicKey,
        sourceToken: Token.Active,
        lamports: BigInteger
    ): SendResult {
        val currentUser = tokenKeyProvider.publicKey

        if (destinationAddress.toBase58().length < PublicKey.PUBLIC_KEY_LENGTH) {
            return SendResult.WrongWallet
        }

        val address = try {
            addressInteractor.findAssociatedAddress(destinationAddress, sourceToken.mintAddress)
        } catch (e: IllegalStateException) {
            return SendResult.WrongWallet
        }

        if (currentUser == address.associatedAddress.toBase58()) {
            return SendResult.Error(R.string.main_send_to_yourself_error)
        }

        val preparedTransaction = buildTransaction(
            address = address,
            sourceToken = sourceToken,
            destinationAddress = destinationAddress,
            lamports = lamports
        )

        val transaction = preparedTransaction.transaction
        val signature = feeRelayerTopUpInteractor.relayTransaction(transaction).firstOrNull().orEmpty()

        return SendResult.Success(signature)
    }

    private suspend fun buildTransaction(
        address: TransactionAddressData,
        sourceToken: Token.Active,
        destinationAddress: PublicKey,
        lamports: BigInteger
    ): PreparedTransaction {
        val userPublicKey = tokenKeyProvider.publicKey.toPublicKey()
        val feePayerPubkey = feeRelayerAccountInteractor.getRelayInfo().feePayerAddress

        val transaction = Transaction()

        var accountsCreationFee: BigInteger = BigInteger.ZERO

        if (address.shouldCreateAssociatedInstruction) {
            Timber.tag(SEND_TAG).d("Associated token account creation needed, adding create instruction")

            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                sourceToken.mintAddress.toPublicKey(),
                address.associatedAddress,
                destinationAddress,
                userPublicKey
            )

            transaction.addInstruction(createAccount)
            accountsCreationFee += amountInteractor.getMinBalanceForRentExemption()
        }

        val instruction = TokenProgram.createTransferCheckedInstruction(
            TokenProgram.PROGRAM_ID,
            sourceToken.publicKey.toPublicKey(),
            sourceToken.mintAddress.toPublicKey(),
            address.associatedAddress,
            userPublicKey,
            lamports,
            sourceToken.decimals
        )

        transaction.addInstruction(instruction)

        val recentBlockHash = rpcRepository.getRecentBlockhash()

        transaction.feePayer = feePayerPubkey
        transaction.recentBlockHash = recentBlockHash.recentBlockhash

        // calculate fee first
        val expectedFee = FeeAmount(
            transaction = transaction.calculateTransactionFee(amountInteractor.getLamportsPerSignature()),
            accountBalances = accountsCreationFee
        )

        val signers = listOf(Account(tokenKeyProvider.secretKey))
        transaction.sign(signers)
        return PreparedTransaction(
            transaction = transaction,
            signers = signers,
            expectedFee = expectedFee
        )
    }
}