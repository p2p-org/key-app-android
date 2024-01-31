package org.p2p.wallet.send.interactor

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.token.findByMintAddress
import org.p2p.core.utils.plusAssign
import org.p2p.wallet.send.interactor.usecase.GetFeesInPayingTokenUseCase
import org.p2p.wallet.send.repository.SendServiceRepository

class FeePayersRepository(
    private val getFeesInPayingTokenUseCase: GetFeesInPayingTokenUseCase,
    private val sendServiceRepository: SendServiceRepository,
    private val dispatchers: CoroutineDispatchers
) {
    /**
     * The request is too complex
     * Wrapped each request into deferred
     * TODO: Create a function to find fees by multiple tokens
     */
    suspend fun findAlternativeFeePayerTokens(
        userTokens: List<Token.Active>,
        feePayerToExclude: Token.Active,
        transactionFeeInSol: BigInteger,
        accountCreationFeeInSol: BigInteger
    ): List<Token.Active> = withContext(dispatchers.io) {
        Timber.i(
            buildString {
                this += "findAlternativeFeePayerTokens: "
                this += "accountCreationFeeInSol = $accountCreationFeeInSol "
                this += "transactionFeeInSol = $transactionFeeInSol"
            }
        )
        val feePayerTokensMints = sendServiceRepository.getCompensationTokens()
        val tokenToExcludeSymbol = feePayerToExclude.tokenSymbol

        val fees: Map<Base58String, BigInteger> = getFeesInPayingTokenUseCase.execute(
            findFeesIn = feePayerTokensMints,
            transactionFeeInSol = transactionFeeInSol,
            accountCreationFeeInSol = accountCreationFeeInSol
        )

        Timber.i(
            "Filtering user tokens for alternative fee payers: $feePayerTokensMints"
        )
        feePayerTokensMints.mapNotNull { userTokens.findByMintAddress(it.base58Value) }
            .filter { token ->
                if (token.tokenSymbol == tokenToExcludeSymbol) {
                    Timber.i("Excluding ${token.mintAddress} ${token.tokenSymbol}")
                    return@filter false
                }

                val totalInSol = transactionFeeInSol + accountCreationFeeInSol
                if (token.isSOL) {
                    Timber.i("Checking SOL as fee payer = ${token.totalInLamports >= totalInSol}")
                    return@filter token.totalInLamports >= totalInSol
                }

                // assuming that all other tokens are SPL
                val feesInSpl = fees[token.mintAddress.toBase58Instance()]
                if (feesInSpl == null) {
                    Timber.i("Fee in SPL not found for ${token.tokenSymbol} in ${fees.keys}")
                    return@filter false
                }
                val isTokenCoversTheFee = token.totalInLamports >= feesInSpl
                if (!isTokenCoversTheFee) {
                    Timber.w(
                        "Token ${token.tokenSymbol} with amount ${token.totalInLamports} " +
                            "can't cover $feesInSpl"
                    )
                }
                isTokenCoversTheFee
            }
            .also { Timber.i("Found alternative feepayer tokens: ${it.map(Token.Active::mintAddress)}") }
    }
}
