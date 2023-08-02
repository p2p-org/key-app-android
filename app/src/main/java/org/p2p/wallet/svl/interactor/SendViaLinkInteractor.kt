package org.p2p.wallet.svl.interactor

import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.OperationType
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.model.types.ConfirmationStatus
import org.p2p.solanaj.programs.MemoProgram
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerViaLinkInteractor
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.wallet.utils.toPublicKey

class SendViaLinkInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionInteractor: TransactionInteractor,
    private val feeRelayerLinkInteractor: FeeRelayerViaLinkInteractor,
    private val addressInteractor: TransactionAddressInteractor,
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val amountRepository: RpcAmountRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor
) {

    /*
     * Initialize fee payer token
     * */
    suspend fun initialize() {
        feeRelayerLinkInteractor.load()
        orcaInfoInteractor.load()
    }

    suspend fun sendTransaction(
        senderAccount: Account? = null,
        destinationAddress: PublicKey,
        token: Token.Active,
        lamports: BigInteger,
        memo: String,
        isSimulation: Boolean,
        shouldCloseAccount: Boolean = false,
        preflightCommitment: ConfirmationStatus = ConfirmationStatus.FINALIZED
    ): String {
        val statistics = FeeRelayerStatistics(
            operationType = OperationType.TRANSFER,
            currency = token.mintAddress
        )

        val feePayer = feeRelayerAccountInteractor.getFeePayerPublicKey()
        val preparedTransaction = createSendTransaction(
            senderAccount = senderAccount,
            destinationAddress = destinationAddress,
            token = token,
            lamports = lamports,
            feePayer = feePayer,
            memo = memo,
            shouldCloseAccount = shouldCloseAccount
        )

        return feeRelayerLinkInteractor.signAndSendTransaction(
            preparedTransaction = preparedTransaction,
            statistics = statistics,
            isRetryEnabled = false,
            isSimulation = isSimulation,
            preflightCommitment = preflightCommitment
        )
    }

    private suspend fun createSendTransaction(
        senderAccount: Account?,
        destinationAddress: PublicKey,
        token: Token.Active,
        lamports: BigInteger,
        feePayer: PublicKey,
        memo: String,
        shouldCloseAccount: Boolean
    ): PreparedTransaction {
        val account = senderAccount ?: Account(tokenKeyProvider.keyPair)

        val preparedTransaction = if (token.isSOL) {
            createSolTransaction(
                account = account,
                destinationAddress = destinationAddress,
                lamports = lamports,
                feePayer = feePayer,
                memo = memo
            )
        } else {
            createSplTransaction(
                account = account,
                mintAddress = token.mintAddress,
                decimals = token.decimals,
                fromPublicKey = token.publicKey,
                destinationAddress = destinationAddress,
                amount = lamports,
                feePayer = feePayer,
                memo = memo,
                shouldCloseAccount = shouldCloseAccount
            )
        }

        return preparedTransaction.also {
            Timber.i("SVL transaction created = \n${it.toFormattedString()}")
        }
    }

    private suspend fun createSolTransaction(
        account: Account,
        destinationAddress: PublicKey,
        lamports: BigInteger,
        feePayer: PublicKey,
        memo: String,
    ): PreparedTransaction {
        val instructions = mutableListOf<TransactionInstruction>()

        instructions += SystemProgram.transfer(
            fromPublicKey = account.publicKey,
            toPublicKey = destinationAddress,
            lamports = lamports
        )

        instructions += MemoProgram.createMemoInstruction(
            signer = account.publicKey,
            memo = memo
        )

        return transactionInteractor.prepareTransaction(
            instructions = instructions,
            signers = listOf(account),
            feePayer = feePayer,
            accountsCreationFee = BigInteger.ZERO
        )
    }

    private suspend fun createSplTransaction(
        account: Account,
        mintAddress: String,
        decimals: Int,
        fromPublicKey: String,
        destinationAddress: PublicKey,
        amount: BigInteger,
        feePayer: PublicKey,
        memo: String,
        shouldCloseAccount: Boolean
    ): PreparedTransaction {
        val splDestinationAddress = addressInteractor.findSplTokenAddressData(
            destinationAddress = destinationAddress,
            mintAddress = mintAddress
        )

        val instructions = mutableListOf<TransactionInstruction>()

        val shouldCreateAccount = splDestinationAddress.shouldCreateAccount
        Timber.i("SVL ATA account state = $splDestinationAddress")
        if (shouldCreateAccount) {
            Timber.i(
                buildString {
                    append("adding ATA creation instruction: ")
                    append("spl_address=${splDestinationAddress.destinationAddress}; ")
                    append("owner=$destinationAddress")
                }
            )
            // we should always create associated token account, since the recipient is a new temporary account user
            instructions += TokenProgram.createAssociatedTokenAccountInstruction(
                /* associatedProgramId = */ TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                /* tokenProgramId = */ TokenProgram.PROGRAM_ID,
                /* mint = */ mintAddress.toPublicKey(),
                /* associatedAccount = */ splDestinationAddress.destinationAddress,
                /* owner = */ destinationAddress,
                /* payer = */ feePayer
            )
        }

        val accountCreationFee = amountRepository.getMinBalanceForRentExemption(ACCOUNT_INFO_DATA_LENGTH)

        instructions += TokenProgram.createTransferCheckedInstruction(
            TokenProgram.PROGRAM_ID,
            fromPublicKey.toPublicKey(),
            mintAddress.toPublicKey(),
            splDestinationAddress.destinationAddress,
            account.publicKey,
            amount,
            decimals
        )

        instructions += MemoProgram.createMemoInstruction(
            signer = account.publicKey,
            memo = memo
        )

        if (shouldCloseAccount) {
            instructions += TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                fromPublicKey.toPublicKey(),
                feePayer,
                account.publicKey,
            )
        }

        return transactionInteractor.prepareTransaction(
            instructions = instructions,
            signers = listOf(account),
            feePayer = feePayer,
            accountsCreationFee = accountCreationFee
        )
    }
}
