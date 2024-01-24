package org.p2p.wallet.send.interactor

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.token.findByMintAddress
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

        Timber.i(
            "Filtering user tokens for alternative fee payers: ${feePayerTokens.map(Token.Active::mintAddress)}"
        )
        feePayerTokens.filter { token ->
            if (token.tokenSymbol == tokenToExcludeSymbol) {
                Timber.i("Excluding ${token.mintAddress} ${token.tokenSymbol}")
                return@filter false
            }

            val totalInSol = transactionFeeInSOL + accountCreationFeeInSOL
            if (token.isSOL) {
                Timber.i("Checking SOL as fee payer = ${token.totalInLamports >= totalInSol}")
                return@filter token.totalInLamports >= totalInSol
            }

            // assuming that all other tokens are SPL
            val feesInSpl = fees[token.tokenSymbol] ?: return@filter run {
                Timber.i("Fee in SPL not found for ${token.tokenSymbol} in ${fees.keys}")
                false
            }
            token.totalInLamports >= feesInSpl.total
        }
            .also { Timber.i("Found alternative feepayer tokens: ${it.map(Token.Active::mintAddress)}") }
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
}
