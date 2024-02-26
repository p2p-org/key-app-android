package org.p2p.wallet.send.interactor

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.token.findByMintAddress
import org.p2p.core.utils.plusAssign
import org.p2p.core.wrapper.eth.helpers.RandomHelper.randomBytes
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.send.interactor.usecase.GetFeesInPayingTokenUseCase
import org.p2p.wallet.send.repository.SendServiceRepository

class FeePayersRepository(
    private val getFeesInPayingTokenUseCase: GetFeesInPayingTokenUseCase,
    private val sendServiceRepository: SendServiceRepository,
    private val dispatchers: CoroutineDispatchers,
    private val tokenKeyProvider: TokenKeyProvider,
) {
    /**
     * The request is too complex
     * Wrapped each request into deferred
     * TODO: Create a function to find fees by multiple tokens
     */
    suspend fun findAlternativeFeePayerTokens(
        userTokens: List<Token.Active>,
        sourceToken: Token.Active,
        sourceTokenAmount: BigInteger,
        useMax: Boolean,
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
        val feePayerTokensMints = sendServiceRepository.getCompensationTokens().toMutableList()
        val tokenToExcludeSymbol = feePayerToExclude.tokenSymbol

        val feesInGivenTokens: MutableMap<Base58String, BigInteger> =
            getFeesInPayingTokenUseCase.execute(
                findFeesIn = feePayerTokensMints,
                transactionFeeInSol = transactionFeeInSol,
                accountCreationFeeInSol = accountCreationFeeInSol
            )
                .toMutableMap()

        // token2022 token rent fees are also include doubled transfer fee
        // so we need to take rent fee for each token separately
        getTokens2022Fees(
            scope = this,
            userTokens = userTokens,
            feePayerTokensMints = feePayerTokensMints.toSet(),
            sourceToken = sourceToken,
            sourceTokenAmount = sourceTokenAmount,
            useMax = useMax,
            accountCreationFeeInSol = accountCreationFeeInSol,
            resultMapRef = feesInGivenTokens
        )

        Timber.i(
            "Filtering user tokens for alternative fee payers: $feePayerTokensMints"
        )
        val userTokensForFee = feePayerTokensMints.mapNotNull { userTokens.findByMintAddress(it.base58Value) }
        userTokensForFee
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
                val feesInSpl = feesInGivenTokens[token.mintAddress.toBase58Instance()]
                if (feesInSpl == null) {
                    Timber.i("Fee in SPL not found for ${token.tokenSymbol} in ${feesInGivenTokens.keys}")
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

    private suspend fun getTokens2022Fees(
        scope: CoroutineScope,
        userTokens: List<Token.Active>,
        feePayerTokensMints: Set<Base58String>,
        sourceToken: Token.Active,
        sourceTokenAmount: BigInteger,
        useMax: Boolean,
        accountCreationFeeInSol: BigInteger,
        resultMapRef: MutableMap<Base58String, BigInteger>
    ) {
        val amount = when {
            useMax -> {
                SendServiceRepository.UINT64_MAX
            }
            sourceToken.isSOL -> {
                maxOf(BigInteger("2"), accountCreationFeeInSol)
            }
            else -> {
                maxOf(BigInteger("2"), sourceTokenAmount)
            }
        }
        userTokens
            .filter { it.isToken2022 }
            .filter { it.mintAddressB58 in feePayerTokensMints }
            .map {
                scope.async {
                    try {
                        it.mintAddressB58 to sendServiceRepository.estimateFees(
                            userWallet = tokenKeyProvider.publicKeyBase58,
                            recipient = randomBytes(32).toBase58Instance(),
                            sourceToken = sourceToken,
                            feePayerToken = it,
                            amount = amount
                        )
                    } catch (e: Throwable) {
                        Timber.i(e, "Send service return error for token ${it.mintAddress}")
                        it.mintAddressB58 to null
                    }
                }
            }
            .awaitAll()
            .forEach {
                if (it.second == null) {
                    // exclude token from selection if send-service error
                    resultMapRef.remove(it.first)
                } else {
                    resultMapRef[it.first] = it.second!!.tokenAccountRent.amount.amount
                }
            }
    }
}
