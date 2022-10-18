package org.p2p.wallet.solend.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.relay.RelayRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.repository.SolendRepository
import java.math.BigInteger

private val COLLATERAL_ACCOUNTS = listOf("SOL", "USDT", "USDC", "BTC", "ETH")

class SolendDepositInteractor(
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val rpcBlockhashRepository: RpcBlockhashRepository,
    private val solendRepository: SolendRepository,
    private val relayRepository: RelayRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun getUserDeposits(tokenSymbols: List<String> = COLLATERAL_ACCOUNTS): List<SolendDepositToken> =
        solendRepository.getUserDeposits(tokenSymbols)

    /**
     * Step 1: Creating the Deposit transaction by [solendRepository.createDepositTransaction].
     * It returns a serialized transaction in Base64
     *
     * Step 2: Sending the serialized transaction in Base64 to [solendRepository.signTransaction] to sign it.
     * It returns a signed, serialized transaction in Base64
     *
     * Step 3: Sending a signed, serialized transaction to the blockhchain directly.
     *
     * @return transactionId
     * */
    suspend fun deposit(token: SolendDepositToken, amountInLamports: BigInteger): String {
        val account = Account(tokenKeyProvider.keypair)
        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val remainingFreeTransactionsCount = freeTransactionFeeLimit.remaining
        val relayProgramId = FeeRelayerProgram.getProgramId(isMainnet = true).toBase58()
        val hasFreeTransactions = freeTransactionFeeLimit.hasFreeTransactions()

        val recentBlockhash = rpcBlockhashRepository.getRecentBlockhash().recentBlockhash
        val realFeePayerAddress = if (hasFreeTransactions) relayInfo.feePayerAddress else account.publicKey

        val serializedTransaction = solendRepository.createDepositTransaction(
            relayProgramId = relayProgramId,
            token = token,
            depositAmount = amountInLamports,
            remainingFreeTransactionsCount = remainingFreeTransactionsCount,
            lendingMarketAddress = null,
            blockhash = recentBlockhash,
            payFeeWithRelay = hasFreeTransactions,
            feePayerToken = null,
            realFeePayerAddress = realFeePayerAddress,
        ) ?: error("Error occurred while creating a withdraw transaction")

        val keypair = account.getEncodedKeyPair()
        val signedTransaction = relayRepository.signTransaction(serializedTransaction, keypair, recentBlockhash)

        return rpcSolanaRepository.sendTransaction(
            serializedTransaction = signedTransaction,
            encoding = Encoding.BASE58
        )
    }
}
