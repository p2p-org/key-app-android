package org.p2p.wallet.send.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerTopUpInteractor
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.rpc.model.FeeRelayerSendFee
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.send.model.CheckAddressResult
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigInteger

class SendInteractor(
    private val addressInteractor: TransactionAddressInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val transactionInteractor: TransactionInteractor,
    private val amountRepository: RpcAmountRepository,
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
        orcaInfoInteractor.load()
    }

    fun setFeePayerToken(newToken: Token.Active) {
        if (!this::feePayerToken.isInitialized) throw IllegalStateException("PayToken is not initialized")
        if (newToken.publicKey.equals(feePayerToken)) return

        feePayerToken = newToken
    }

    // Fees calculator
    suspend fun calculateFeesForFeeRelayer(
        token: Token.Active,
        receiver: String?,
        networkType: NetworkType
    ): FeeRelayerSendFee? {

        return when (networkType) {
            NetworkType.BITCOIN ->
                FeeRelayerSendFee(
                    feeInSol = BigInteger.valueOf(20000L),
                    feeInPayingToken = null
                )
            NetworkType.SOLANA -> {
                if (receiver.isNullOrEmpty() || token.isSOL) return null

                val lamportsPerSignature: BigInteger = amountRepository.getLamportsPerSignature(null)
                val minRentExemption: BigInteger = amountRepository.getMinBalanceForRentExemption()

                var transactionFee: BigInteger = BigInteger.ZERO

                // owner's signature
                transactionFee += lamportsPerSignature

                // feePayer's signature
                if (!feePayerToken.isSOL) {
                    transactionFee += lamportsPerSignature
                }

                val shouldCreateAccount = if (token.mintAddress != WRAPPED_SOL_MINT) {
                    addressInteractor.findSplTokenAddressData(
                        mintAddress = token.mintAddress,
                        destinationAddress = receiver.toPublicKey()
                    ).shouldCreateAccount
                } else false

                val expectedFee = FeeAmount(
                    transaction = transactionFee,
                    accountBalances = if (shouldCreateAccount) minRentExemption else BigInteger.ZERO
                )

                val fees = feeRelayerTopUpInteractor.calculateNeededTopUpAmount(expectedFee)

                return FeeRelayerSendFee(
                    feeInSol = fees.total,
                    feeInPayingToken = getFeesInPayingToken(fees.total)
                )
            }
        }
    }

    suspend fun getFeeTokenAccounts(fromPublicKey: String): List<Token.Active> =
        feeRelayerAccountInteractor.getFeeTokenAccounts(fromPublicKey)

    suspend fun getFreeTransactionsInfo(): FreeTransactionFeeLimit {
        return feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
    }

    suspend fun checkAddress(destinationAddress: PublicKey, token: Token.Active): CheckAddressResult =
        try {
            val isSolAddress = addressInteractor.isSolAddress(destinationAddress.toBase58())
            if (isSolAddress && token.isSOL) {
                CheckAddressResult.AccountExists
            } else {
                val address = addressInteractor.findSplTokenAddressData(
                    destinationAddress = destinationAddress,
                    mintAddress = token.mintAddress
                )
                val accountAddress = address.destinationAddress.toBase58()
                if (address.shouldCreateAccount) {
                    Timber.tag("Address").d("Account should be created: $accountAddress")
                    CheckAddressResult.NewAccountNeeded(feePayerToken)
                } else {
                    Timber.tag("Address").d("Account exists: $accountAddress")
                    CheckAddressResult.AccountExists
                }
            }
        } catch (e: IllegalStateException) {
            CheckAddressResult.InvalidAddress
        }

    suspend fun sendTransaction(
        destinationAddress: PublicKey,
        token: Token.Active,
        lamports: BigInteger
    ): String {

        val preparedTransaction = prepareForSending(
            token = token,
            receiver = destinationAddress.toBase58(),
            amount = lamports,
        )

        return if (feePayerToken.mintAddress != WRAPPED_SOL_MINT) {
            // use fee relayer
            feeRelayerInteractor.topUpAndRelayTransaction(
                preparedTransaction = preparedTransaction,
                payingFeeToken = TokenInfo(feePayerToken.publicKey, feePayerToken.mintAddress)
            ).firstOrNull().orEmpty()
        } else {
            // send normally, paid by SOL
            transactionInteractor.serializeAndSend(
                preparedTransaction = preparedTransaction,
                isSimulation = false
            )
        }
    }

    private suspend fun getFeesInPayingToken(
        feeInSOL: BigInteger
    ): BigInteger {
        if (feePayerToken.isSOL) return feeInSOL

        val feeInPayingToken = feeRelayerInteractor.calculateFeeInPayingToken(
            feeInSOL = FeeAmount(accountBalances = feeInSOL),
            payingFeeTokenMint = feePayerToken.mintAddress
        )
        return feeInPayingToken.total
    }

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
            throw IllegalStateException("You can not send tokens to yourself")
        }

        val (feePayer, useFeeRelayer) = if (feePayerToken.isSOL) {
            null to false
        } else {
            val feePayer = feeRelayerAccountInteractor.getFeePayerPublicKey()
            feePayer to true
        }

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
        val account = Account(tokenKeyProvider.secretKey)

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
        val account = Account(tokenKeyProvider.secretKey)

        val feePayer = feePayerPublicKey ?: account.publicKey

        val minRentExemption =
            minBalanceForRentExemption ?: amountRepository.getMinBalanceForRentExemption()

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
            Timber.tag(SEND_TAG).d("Associated token account creation needed, adding create instruction")

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
            realDestination = splDestinationAddress.destinationAddress.toBase58()
        }

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
}
