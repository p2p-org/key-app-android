package org.p2p.wallet.solend.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.relay.RelayRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.repository.SolendRepository
import org.p2p.wallet.utils.toBase58Instance
import java.math.BigInteger

class SolendWithdrawInteractor(
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val rpcBlockhashRepository: RpcBlockhashRepository,
    private val solendRepository: SolendRepository,
    private val relayRepository: RelayRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun withdraw(token: SolendDepositToken, amountInLamports: BigInteger): String {
        val account = Account(tokenKeyProvider.keypair)
        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val remainingFreeTransactionsCount = freeTransactionFeeLimit.remaining
        val relayProgramId = FeeRelayerProgram.getProgramId(isMainnet = true).toBase58()
        val hasFreeTransactions = freeTransactionFeeLimit.hasFreeTransactions()

        val recentBlockhash = rpcBlockhashRepository.getRecentBlockhash().recentBlockhash
        val realFeePayerAddress = if (hasFreeTransactions) relayInfo.feePayerAddress else account.publicKey

        val serializedTransaction = solendRepository.createWithdrawTransaction(
            relayProgramId = relayProgramId,
            token = token,
            withdrawAmount = amountInLamports,
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

    private suspend fun getDepositFee(amountInLamports: BigInteger, symbol: String): FeeAmount {
        val owner = tokenKeyProvider.publicKey.toBase58Instance()
        val feeInSol = solendRepository.getDepositFee(
            owner = owner,
            tokenAmount = amountInLamports,
            tokenSymbol = symbol
        )

        val hasFreeTransactions = feeRelayerAccountInteractor.getFreeTransactionFeeLimit().hasFreeTransactions()
        return FeeAmount(
            transaction = if (hasFreeTransactions) BigInteger.ZERO else feeInSol.rent,
            accountBalances = feeInSol.accountCreationFee
        )
    }
}
