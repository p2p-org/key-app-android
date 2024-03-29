package org.p2p.wallet.svl.interactor

import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.BuildConfig.svlMemoClaim
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.core.utils.isMoreThan
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.OperationType
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.model.types.ConfirmationStatus
import org.p2p.solanaj.programs.MemoProgram
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerViaLinkInteractor
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.interactor.TransactionInteractor
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.send.model.SEND_LINK_FORMAT
import org.p2p.wallet.send.model.TemporaryAccount
import org.p2p.wallet.svl.model.SendLinkGenerator
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toPublicKey

class ReceiveViaLinkError(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable()

class ReceiveViaLinkInteractor(
    private val rpcAccountRepository: RpcAccountRepository,
    private val rpcBalanceRepository: RpcBalanceRepository,
    private val userLocalRepository: UserLocalRepository,
    private val tokenServiceRepository: TokenServiceRepository,
    private val addressInteractor: TransactionAddressInteractor,
    private val transactionInteractor: TransactionInteractor,
    private val userTokensInteractor: UserTokensInteractor,
    private val feeRelayerLinkInteractor: FeeRelayerViaLinkInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val amountRepository: RpcAmountRepository
) {

    companion object {
        private const val TAG = "ReceiveViaLinkInteractor"
    }

    suspend fun parseAccountFromLink(link: SendViaLinkWrapper): TemporaryAccountState {
        val seedCode = link.link.substringAfterLast(SEND_LINK_FORMAT)
            .toList()
            .map(Char::toString)

        if (!SendLinkGenerator.isValidSeedCode(seedCode)) {
            return TemporaryAccountState.BrokenLink
        }

        val temporaryAccount = try {
            SendLinkGenerator.parseTemporaryAccount(seedCode)
        } catch (e: Throwable) {
            Timber.e(ReceiveViaLinkError("Failed to parse temporary account", e))
            return TemporaryAccountState.BrokenLink
        }

        val tokenAccounts = rpcAccountRepository.getTokenAccountsByOwner(temporaryAccount.publicKey)
        val activeAccount = tokenAccounts.accounts.firstOrNull {
            it.account.data.parsed.info.tokenAmount.amount.toBigInteger().isMoreThan(BigInteger.ZERO)
        } ?: return checkSolBalance(temporaryAccount)

        val info = activeAccount.account.data.parsed.info
        val programId = activeAccount.account.owner
        val tokenData = userLocalRepository.findTokenData(info.mint) ?: run {
            Timber.e(ReceiveViaLinkError("No token data found for mint ${info.mint}"))
            return TemporaryAccountState.ParsingFailed
        }

        val tokenPrice = fetchPriceForToken(tokenData.mintAddress)

        val token = TokenConverter.fromNetwork(
            programId = programId,
            account = activeAccount,
            tokenMetadata = tokenData,
            price = tokenPrice
        )
        return TemporaryAccountState.Active(
            account = temporaryAccount,
            token = token
        )
    }

    // if no SPL accounts found, check SOL balance
    private suspend fun checkSolBalance(temporaryAccount: TemporaryAccount): TemporaryAccountState {
        val solBalance = rpcBalanceRepository.getBalance(temporaryAccount.publicKey)
        if (solBalance == 0L) {
            return TemporaryAccountState.EmptyBalance
        }

        val tokenData = userLocalRepository.findTokenData(WRAPPED_SOL_MINT)
            ?: return TemporaryAccountState.ParsingFailed

        val solPrice = fetchPriceForToken(tokenData.mintAddress)

        val token = Token.createSOL(
            publicKey = temporaryAccount.publicKey.toBase58(),
            tokenMetadata = tokenData,
            amount = solBalance,
            solPrice = solPrice?.usdRate
        )
        return TemporaryAccountState.Active(
            account = temporaryAccount,
            token = token
        )
    }

    private suspend fun fetchPriceForToken(mintAddress: String): TokenServicePrice? {
        return kotlin.runCatching {
            tokenServiceRepository.getTokenPriceByAddress(
                tokenAddress = mintAddress,
                networkChain = TokenServiceNetwork.SOLANA,
                forceRemote = true
            )
        }
            .onFailure { Timber.i(it) }
            .getOrNull() // can be skipped if there error, that's ok
    }

    suspend fun receiveTransfer(
        temporaryAccount: TemporaryAccount,
        token: Token.Active,
        recipient: PublicKey
    ): String {
        val senderAccount = Account(temporaryAccount.keypair)

        val statistics = FeeRelayerStatistics(
            operationType = OperationType.TRANSFER,
            currency = token.mintAddress
        )

        val feePayer = feeRelayerAccountInteractor.getFeePayerPublicKey()
        Timber.i("receiveTransfer called: ${senderAccount.publicKey}; d_address=$senderAccount")
        val preparedTransaction = createSendTransaction(
            senderAccount = senderAccount,
            destinationAddress = recipient,
            token = token,
            lamports = token.totalInLamports,
            feePayer = feePayer,
            memo = svlMemoClaim,
            shouldCloseAccount = true
        )

        return feeRelayerLinkInteractor.signAndSendTransaction(
            preparedTransaction = preparedTransaction,
            statistics = statistics,
            isRetryEnabled = false,
            isSimulation = false,
            preflightCommitment = ConfirmationStatus.CONFIRMED
        )
    }

    private suspend fun createSendTransaction(
        senderAccount: Account,
        destinationAddress: PublicKey,
        token: Token.Active,
        lamports: BigInteger,
        feePayer: PublicKey,
        memo: String,
        shouldCloseAccount: Boolean
    ): PreparedTransaction {

        val preparedTransaction = if (token.isSOL) {
            createSolTransaction(
                account = senderAccount,
                destinationAddress = destinationAddress,
                lamports = lamports,
                feePayer = feePayer,
                memo = memo
            )
        } else {
            createSplTransaction(
                account = senderAccount,
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

        return preparedTransaction
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

        val hasUserToken = userTokensInteractor.hasUserToken(mintAddress)

        Timber.tag(TAG).d("The user has the same token: $hasUserToken")

        if (!hasUserToken) {
            // we should always create associated token account, since the recipient is a new temporary account user
            instructions += TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                mintAddress.toPublicKey(),
                splDestinationAddress.destinationAddress,
                destinationAddress,
                feePayer
            )
        }

        val accountCreationFee =
            amountRepository.getMinBalanceForRentExemption(TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH)

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
}
