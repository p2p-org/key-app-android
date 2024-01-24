package org.p2p.wallet.send.interactor

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.crypto.toBase64Instance
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.token.findByMintAddress
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.OperationType
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.core.toBase58Instance
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.RelayInfo
import org.p2p.wallet.feerelayer.model.TokenAccount
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.send.interactor.usecase.GetFeesInPayingTokenUseCase
import org.p2p.wallet.send.model.SendFatalError
import org.p2p.wallet.send.model.SendTransactionFailed
import org.p2p.wallet.send.model.send_service.SendFeePayerMode
import org.p2p.wallet.send.model.send_service.SendRentPayerMode
import org.p2p.wallet.send.model.send_service.SendTransferMode
import org.p2p.wallet.send.repository.SendServiceRepository
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.wallet.utils.toPublicKey

private const val TAG = "SendInteractor"

class SendInteractor(
    private val addressInteractor: TransactionAddressInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val transactionInteractor: TransactionInteractor,
    private val amountRepository: RpcAmountRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val dispatchers: CoroutineDispatchers,
    private val sendServiceRepository: SendServiceRepository,
    private val getFeesInPayingTokenUseCase: GetFeesInPayingTokenUseCase,
) {

    /*
    * If transaction will need to create a new account,
    * then the fee for account creation will be paid via this token
    * */
    private lateinit var feePayerToken: Token.Active

    /*
    * Initialize fee payer token
    * */
    suspend fun initialize(token: Token.Active) {
        feePayerToken = token
        feeRelayerInteractor.load()
        orcaInfoInteractor.load()
    }

    fun setFeePayerToken(newToken: Token.Active) {
        if (!::feePayerToken.isInitialized) throw SendFatalError("FeePayerToken is not initialized")
        if (newToken.publicKey == feePayerToken.publicKey) return

        feePayerToken = newToken
        Timber.tag(TAG).i("Fee payer token switched: ${newToken.mintAddress}")
    }

    fun getFeePayerToken(): Token.Active = feePayerToken

    /*
    * The request is too complex
    * Wrapped each request into deferred
    * TODO: Create a function to find fees by multiple tokens
    * */
    suspend fun findAlternativeFeePayerTokens(
        userTokens: List<Token.Active>,
        feePayerToExclude: Token.Active,
        transactionFeeInSOL: BigInteger,
        accountCreationFeeInSOL: BigInteger
    ): List<Token.Active> = withContext(dispatchers.io) {
        val feePayerTokens = sendServiceRepository.getCompensationTokens()
            .mapNotNull { userTokens.findByMintAddress(it.base58Value) }

        val tokenToExcludeSymbol = feePayerToExclude.tokenSymbol
        val fees = feePayerTokens.map { token ->
            // converting SOL fee in token lamports to verify the balance coverage
            async {
                getFeesInPayingTokenUseCase.executeNullable(
                    token = token,
                    transactionFeeInSOL = transactionFeeInSOL,
                    accountCreationFeeInSOL = accountCreationFeeInSOL
                )
            }
        }
            .awaitAll()
            .filterNotNull()
            .toMap()

        Timber.tag(TAG).i(
            "Filtering user tokens for alternative fee payers: ${feePayerTokens.map(Token.Active::mintAddress)}"
        )
        feePayerTokens.filter { token ->
            if (token.tokenSymbol == tokenToExcludeSymbol) {
                Timber.tag(TAG).i("Excluding ${token.mintAddress} ${token.tokenSymbol}")
                return@filter false
            }

            val totalInSol = transactionFeeInSOL + accountCreationFeeInSOL
            if (token.isSOL) {
                Timber.tag(TAG).i("Checking SOL as fee payer = ${token.totalInLamports >= totalInSol}")
                return@filter token.totalInLamports >= totalInSol
            }

            // assuming that all other tokens are SPL
            val feesInSpl = fees[token.tokenSymbol] ?: return@filter run {
                Timber.tag(TAG).i("Fee in SPL not found for ${token.tokenSymbol} in ${fees.keys}")
                false
            }
            token.totalInLamports >= feesInSpl.total
        }.also {
            Timber.tag(TAG).i("Found alternative feepayer tokens: ${it.map(Token.Active::mintAddress)}")
        }
    }

    suspend fun findSupportedFeePayerTokens(
        userTokens: List<Token.Active>,
        transactionFeeInSOL: BigInteger,
        accountCreationFeeInSOL: BigInteger
    ): List<Token.Active> = withContext(dispatchers.io) {
        val fees = userTokens
            .map { token ->
                // converting SOL fee in token lamports to verify the balance coverage
                async {
                    getFeesInPayingTokenUseCase.executeNullable(
                        token = token,
                        transactionFeeInSOL = transactionFeeInSOL,
                        accountCreationFeeInSOL = accountCreationFeeInSOL
                    )
                }
            }
            .awaitAll()
            .filterNotNull()
            .toMap()

        userTokens.filter { token ->
            val totalInSol = transactionFeeInSOL + accountCreationFeeInSOL
            if (token.isSOL) return@filter token.totalInLamports >= totalInSol

            // assuming that all other tokens are SPL
            val feesInSpl = fees[token.tokenSymbol] ?: return@filter false
            token.totalInLamports >= feesInSpl.total
        }
    }

    suspend fun getFeeTokenAccounts(fromPublicKey: String): List<Token.Active> =
        feeRelayerAccountInteractor.getFeeTokenAccounts(fromPublicKey)

    suspend fun getFreeTransactionsInfo(): TransactionFeeLimits =
        feeRelayerAccountInteractor.getFreeTransactionFeeLimit()

    @Deprecated("Use Send-Service method")
    suspend fun sendTransaction(
        destinationAddress: PublicKey,
        token: Token.Active,
        lamports: BigInteger
    ): String = withContext(dispatchers.io) {
        Timber.tag(TAG).i("Start sendTransaction")

        val preparedTransaction = prepareForSending(
            token = token,
            receiver = destinationAddress.toBase58(),
            amount = lamports
        )

        val transactionPublicKey = preparedTransaction.transaction.signature?.publicKey?.toBase58()
        Timber.tag(TAG).i("Send transaction prepared: $transactionPublicKey")

        try {
            if (shouldUseNativeSwap(feePayerToken.mintAddress)) {
                Timber.tag(TAG).i("Using native swap")
                // send normally, paid by SOL
                transactionInteractor.serializeAndSend(
                    transaction = preparedTransaction.transaction,
                    isSimulation = false
                )
            } else {
                Timber.tag(TAG).i("Using FeeRelayer for send")
                // use fee relayer
                val statistics = FeeRelayerStatistics(
                    operationType = OperationType.TRANSFER,
                    currency = token.mintAddress
                )
                feeRelayerInteractor.topUpAndRelayTransaction(
                    preparedTransaction = preparedTransaction,
                    payingFeeToken = TokenAccount(feePayerToken.publicKey, feePayerToken.mintAddress),
                    additionalPaybackFee = BigInteger.ZERO,
                    statistics = statistics
                )
            }
        } catch (error: Throwable) {
            Timber.tag(TAG).i(error, "Failed sending transaction")
            throw SendTransactionFailed(transactionPublicKey.orEmpty(), error)
        }
    }

    suspend fun sendTransactionV2(
        destinationAddress: PublicKey,
        token: Token.Active,
        lamports: BigInteger
    ): String = withContext(dispatchers.io) {
        val sender = token.publicKey

        require(sender != destinationAddress.toBase58()) {
            "You can not send tokens to yourself"
        }

        // selecting fee payer
        val feePayerMode: Pair<SendFeePayerMode, Base58String?> = decideWhoPaysNetworkFee()
        val rentPayerMode: Pair<SendRentPayerMode, Base58String?> = decideWhoPaysAccountCreationFees(
            destinationAddress = destinationAddress,
            tokenMintAddress = token.mintAddress,
            programId = token.programId?.toPublicKey() ?: TokenProgram.PROGRAM_ID
        )

        val signer = Account(tokenKeyProvider.keyPair)

        Timber.i("Generating transaction")
        Timber.i("FeePayerMode: ${feePayerMode.first} - ${feePayerMode.second}")
        Timber.i("RentPayerMode: ${rentPayerMode.first} - ${rentPayerMode.second}")

        try {
            val generatedTransaction = sendServiceRepository.generateTransaction(
                // user_wallet must be signer, not a token account
                userWallet = signer.publicKey.toBase58Instance(),
                amountLamports = lamports,
                recipient = destinationAddress.toBase58Instance(),
                tokenMint = if (token.isSOL) null else token.mintAddress.toBase58Instance(),
                transferMode = SendTransferMode.ExactOut,

                feePayerMode = feePayerMode.first,
                customFeePayerTokenMint = feePayerMode.second,

                rentPayerMode = rentPayerMode.first,
                customRentPayerTokenMint = rentPayerMode.second
            )

            val signedTransaction = transactionInteractor.signGeneratedTransaction(
                signer = signer,
                generatedTransaction = generatedTransaction,
            )
            transactionInteractor.sendTransaction(
                signedTransaction = signedTransaction.toBase64Instance(),
                isSimulation = false,
            )
        } catch (error: Throwable) {
            Timber.tag(TAG).i(error, "Failed sending transaction")
            // todo: SendTransactionFailed first argument is signature, not public key
            //       figure out what do we really need out of this
            throw SendTransactionFailed(signer.publicKey.toBase58(), error)
        }
    }

    suspend fun getMinRelayRentExemption(): BigInteger =
        feeRelayerAccountInteractor.getRelayInfo().minimumRelayAccountRent

    suspend fun getRelayInfo(): RelayInfo =
        feeRelayerAccountInteractor.getRelayInfo()

    suspend fun getUserRelayAccount(): RelayAccount =
        feeRelayerAccountInteractor.getUserRelayAccount()

    private suspend fun prepareForSending(
        token: Token.Active,
        receiver: String,
        amount: BigInteger,
        recentBlockhash: String? = null,
        lamportsPerSignature: BigInteger? = null,
        minRentExemption: BigInteger? = null
    ): PreparedTransaction {
        val sender = token.publicKey

        if (sender == receiver) {
            error("You can not send tokens to yourself")
        }

        val (feePayer, useFeeRelayer) = if (shouldUseNativeSwap(feePayerToken.mintAddress)) {
            null to false
        } else {
            val feePayer = feeRelayerAccountInteractor.getFeePayerPublicKey()
            feePayer to true
        }

        Timber.tag(TAG).i("feePayer = $feePayer; useFeeRelayer=$useFeeRelayer")

        return when {
            token.isSOL -> {
                prepareNativeSol(
                    destinationAddress = receiver.toPublicKey(),
                    lamports = amount,
                    feePayerPublicKey = feePayer,
                    recentBlockhash = recentBlockhash,
                    lamportsPerSignature = lamportsPerSignature
                )
            }
            token.isToken2022 -> {
                prepareSplToken(
                    mintAddress = token.mintAddress,
                    decimals = token.decimals,
                    fromPublicKey = sender,
                    destinationAddress = receiver,
                    amount = amount,
                    feePayerPublicKey = feePayer,
                    transferChecked = useFeeRelayer, // create transferChecked instruction when using fee relayer
                    recentBlockhash = recentBlockhash,
                    lamportsPerSignature = lamportsPerSignature,
                    minBalanceForRentExemption = minRentExemption,
                    programId = TokenProgram.TOKEN_2022_PROGRAM_ID,
                ).first
            }
            else -> {
                prepareSplToken(
                    mintAddress = token.mintAddress,
                    decimals = token.decimals,
                    fromPublicKey = sender,
                    destinationAddress = receiver,
                    amount = amount,
                    feePayerPublicKey = feePayer,
                    transferChecked = useFeeRelayer, // create transferChecked instruction when using fee relayer
                    recentBlockhash = recentBlockhash,
                    lamportsPerSignature = lamportsPerSignature,
                    minBalanceForRentExemption = minRentExemption
                ).first
            }
        }
    }

    private suspend fun prepareNativeSol(
        destinationAddress: PublicKey,
        lamports: BigInteger,
        feePayerPublicKey: PublicKey? = null,
        recentBlockhash: String? = null,
        lamportsPerSignature: BigInteger? = null
    ): PreparedTransaction {
        Timber.tag(TAG).i("preparing native sol to send to $destinationAddress")

        val account = Account(tokenKeyProvider.keyPair)

        val instruction = SystemProgram.transfer(
            fromPublicKey = account.publicKey,
            toPublicKey = destinationAddress,
            lamports = lamports
        )

        val feePayer = feePayerPublicKey ?: account.publicKey

        return transactionInteractor.prepareTransaction(
            instructions = listOf(instruction),
            signers = listOf(account),
            feePayer = feePayer,
            accountsCreationFee = BigInteger.ZERO,
            recentBlockhash = recentBlockhash,
            lamportsPerSignature = lamportsPerSignature
        ).also {
            Timber.tag(TAG).i(
                buildString {
                    append("SOL transaction prepared: ")
                    append("transaction=${it.toFormattedString()};\n")
                    append("l_per_sig=$lamportsPerSignature; ")
                    append("real_dist=$destinationAddress")
                }
            )
        }
    }

    private suspend fun prepareSplToken(
        mintAddress: String,
        decimals: Int,
        fromPublicKey: String,
        destinationAddress: String,
        amount: BigInteger,
        transferChecked: Boolean,
        feePayerPublicKey: PublicKey? = null,
        recentBlockhash: String? = null,
        lamportsPerSignature: BigInteger? = null,
        minBalanceForRentExemption: BigInteger? = null,
        programId: PublicKey = TokenProgram.PROGRAM_ID,
    ): Pair<PreparedTransaction, String> {
        Timber.tag(TAG).i("preparing spl token: $mintAddress")

        val account = Account(tokenKeyProvider.keyPair)

        val feePayer = feePayerPublicKey ?: account.publicKey

        val minRentExemption = minBalanceForRentExemption
            ?: amountRepository.getMinBalanceForRentExemption(ACCOUNT_INFO_DATA_LENGTH)

        val splDestinationAddress = addressInteractor.findSplTokenAddressData(
            destinationAddress = destinationAddress.toPublicKey(),
            mintAddress = mintAddress,
            programId = programId,
        )

        // get address
        val toPublicKey = splDestinationAddress.destinationAddress

        val instructions = mutableListOf<TransactionInstruction>()

        var accountsCreationFee: BigInteger = BigInteger.ZERO
        // create associated token address
        if (splDestinationAddress.shouldCreateAccount) {
            Timber.tag(TAG).i("Associated token account creation needed, adding create instruction")

            val owner = destinationAddress.toPublicKey()

            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                /* associatedProgramId = */ TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                /* tokenProgramId = */ programId,
                /* mint = */ mintAddress.toPublicKey(),
                /* associatedAccount = */ toPublicKey,
                /* owner = */ owner,
                /* payer = */ feePayer
            )

            instructions += createAccount

            accountsCreationFee += minRentExemption
        }

        // send instruction
        val transferInstruction = if (transferChecked) {
            Timber.tag(TAG).i("adding transferChecked instruction")
            TokenProgram.createTransferCheckedInstruction(
                programId,
                fromPublicKey.toPublicKey(),
                mintAddress.toPublicKey(),
                splDestinationAddress.destinationAddress,
                account.publicKey,
                amount,
                decimals
            )
        } else {
            Timber.tag(TAG).i("adding regular transfer instruction")
            TokenProgram.transferInstruction(
                programId,
                fromPublicKey.toPublicKey(),
                toPublicKey,
                account.publicKey,
                amount
            )
        }

        instructions += transferInstruction

        var realDestination = destinationAddress
        if (!splDestinationAddress.shouldCreateAccount) {
            Timber.tag(TAG).i("No need to create account for spl destination address")
            realDestination = splDestinationAddress.destinationAddress.toBase58()
        }
        Timber.tag(TAG).i("realDestination = $realDestination")

        val preparedTransaction = transactionInteractor.prepareTransaction(
            instructions = instructions,
            signers = listOf(account),
            feePayer = feePayer,
            accountsCreationFee = accountsCreationFee,
            recentBlockhash = recentBlockhash,
            lamportsPerSignature = lamportsPerSignature
        )
        Timber.tag(TAG).i(
            buildString {
                append("SPL transaction prepared: ")
                append("transaction = ${preparedTransaction.toFormattedString()};\n")
                append("l_per_sig=$lamportsPerSignature; ")
                append("real_dist=$realDestination")
            }
        )

        return preparedTransaction to realDestination
    }

    /*
    * When free transaction is not available and user is paying with sol,
    * let him do this the normal way (don't use fee relayer)
    * */
    private suspend fun shouldUseNativeSwap(payingTokenMint: String): Boolean {
        val noFreeTransactionsLeft = feeRelayerAccountInteractor.getFreeTransactionFeeLimit().remaining == 0
        val isSol = payingTokenMint == WRAPPED_SOL_MINT
        return noFreeTransactionsLeft && isSol
    }

    private suspend fun decideWhoPaysNetworkFee(): Pair<SendFeePayerMode, Base58String?> {
        // deciding what fees user will pay
        val useFeeRelayer = !shouldUseNativeSwap(feePayerToken.mintAddress)

        return when {
            useFeeRelayer -> {
                SendFeePayerMode.Service to null
            }
            feePayerToken.isSOL -> {
                SendFeePayerMode.UserSol to null
            }
            else -> {
                SendFeePayerMode.Custom to feePayerToken.mintAddress.toBase58Instance()
            }
        }
    }

    private suspend fun decideWhoPaysAccountCreationFees(
        destinationAddress: PublicKey,
        tokenMintAddress: String,
        programId: PublicKey,
    ): Pair<SendRentPayerMode, Base58String?> {
        val shouldCreateTokenAccount = addressInteractor.findSplTokenAddressData(
            destinationAddress = destinationAddress,
            mintAddress = tokenMintAddress,
            programId = programId,
        ).shouldCreateAccount

        return if (feePayerToken.isSOL || !shouldCreateTokenAccount) {
            SendRentPayerMode.UserSol to null
        } else {
            SendRentPayerMode.Custom to feePayerToken.mintAddress.toBase58Instance()
        }
    }
}
