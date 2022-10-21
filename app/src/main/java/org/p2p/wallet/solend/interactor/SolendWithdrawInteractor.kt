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
        val account = Account(tokenKeyProvider.keyPair)
        val ownerAddress = account.publicKey.toBase58().toBase58Instance()
        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()

        val remainingFreeTransactionsCount = freeTransactionFeeLimit.remaining
        val relayProgramId = FeeRelayerProgram.getProgramId(isMainnet = true).toBase58()

        // todo: use `hasFreeTransactions` when fee relayer is fixed
        val hasFreeTransactions = false /* freeTransactionFeeLimit.hasFreeTransactions() */
        val realFeePayerAddress = if (hasFreeTransactions) relayInfo.feePayerAddress else account.publicKey

        val recentBlockhash = rpcBlockhashRepository.getRecentBlockhash().recentBlockhash
        val serializedTransaction = solendRepository.createWithdrawTransaction(
            relayProgramId = relayProgramId,
            ownerAddress = ownerAddress,
            token = token,
            withdrawAmount = amountInLamports,
            remainingFreeTransactionsCount = 0,
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

    suspend fun calculateWithdrawFee(amountInLamports: BigInteger, token: SolendDepositToken.Active): FeeAmount {
        val account = Account(tokenKeyProvider.keyPair)

        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()

        val hasFreeTransactions = freeTransactionFeeLimit.hasFreeTransactions()
        val feePayer = if (hasFreeTransactions) relayInfo.feePayerAddress else account.publicKey
        val fee = solendRepository.getWithdrawFee(
            owner = account.publicKey.toBase58().toBase58Instance(),
            feePayer = feePayer.toBase58().toBase58Instance(),
            tokenAmount = amountInLamports,
            tokenSymbol = token.tokenSymbol
        )

        return FeeAmount(
            transaction = fee.rent,
            accountBalances = fee.accountCreationFee
        )
    }

    private suspend fun getDepositFee(amountInLamports: BigInteger, symbol: String): FeeAmount {
        val owner = tokenKeyProvider.publicKey.toBase58Instance()
        val feeInSol = solendRepository.getDepositFee(
            owner = owner,
            feePayer = owner,
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
