package org.p2p.wallet.newsend.interactor

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.OperationType
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
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
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.newsend.model.SendFatalError
import org.p2p.wallet.newsend.model.SendTransactionFailed
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
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
    private val dispatchers: CoroutineDispatchers
) {

    /*
    * If transaction will need to create a new account,
    * then the fee for account creation will be paid via this token
    * */
    @Deprecated("fee payer token will be cached in presenter")
    private lateinit var feePayerToken: Token.Active

    /*
    * Initialize fee payer token
    * */
    suspend fun initialize() {
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

    fun switchFeePayerToSol(solToken: Token.Active?) {
        solToken?.let(::setFeePayerToken)
    }

    suspend fun getFeeTokenAccounts(fromPublicKey: String): List<Token.Active> =
        feeRelayerAccountInteractor.getFeeTokenAccounts(fromPublicKey)

    suspend fun getFreeTransactionsInfo(useCache: Boolean): TransactionFeeLimits =
        feeRelayerAccountInteractor.getFreeTransactionFeeLimit(useCache)

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

        return if (token.isSOL) {
            prepareNativeSol(
                destinationAddress = receiver.toPublicKey(),
                lamports = amount,
                feePayerPublicKey = feePayer,
                recentBlockhash = recentBlockhash,
                lamportsPerSignature = lamportsPerSignature
            )
        } else {
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

    private suspend fun prepareNativeSol(
        destinationAddress: PublicKey,
        lamports: BigInteger,
        feePayerPublicKey: PublicKey? = null,
        recentBlockhash: String? = null,
        lamportsPerSignature: BigInteger? = null
    ): PreparedTransaction {
        Timber.tag(TAG).i("preparing native sol")

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
        )
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
        minBalanceForRentExemption: BigInteger? = null
    ): Pair<PreparedTransaction, String> {
        Timber.tag(TAG).i("preparing spl token: $mintAddress")

        val account = Account(tokenKeyProvider.keyPair)

        val feePayer = feePayerPublicKey ?: account.publicKey

        val minRentExemption =
            minBalanceForRentExemption ?: amountRepository.getMinBalanceForRentExemption(ACCOUNT_INFO_DATA_LENGTH)

        val splDestinationAddress = addressInteractor.findSplTokenAddressData(
            destinationAddress = destinationAddress.toPublicKey(),
            mintAddress = mintAddress
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
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                mintAddress.toPublicKey(),
                toPublicKey,
                owner,
                feePayer
            )

            instructions += createAccount

            accountsCreationFee += minRentExemption
        }

        // send instruction
        val instruction = if (transferChecked) {
            TokenProgram.createTransferCheckedInstruction(
                TokenProgram.PROGRAM_ID,
                fromPublicKey.toPublicKey(),
                mintAddress.toPublicKey(),
                splDestinationAddress.destinationAddress,
                account.publicKey,
                amount,
                decimals
            )
        } else {
            TokenProgram.transferInstruction(
                TokenProgram.PROGRAM_ID,
                fromPublicKey.toPublicKey(),
                toPublicKey,
                account.publicKey,
                amount
            )
        }

        instructions += instruction

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
}
