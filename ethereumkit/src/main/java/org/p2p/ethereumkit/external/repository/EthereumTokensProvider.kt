package org.p2p.ethereumkit.external.repository

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.external.price.PriceRepository

private const val REFRESH_JOB_DELAY_IN_MILLIS = 30_000L

private const val TAG = "EthereumTokenProvider"

class EthereumTokensProvider(
    private val repository: EthereumRepository,
    private val dispatchers: CoroutineDispatchers,
    private val priceRepository: PriceRepository,
) : CoroutineScope {


    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io
    private val erC20Tokens: MutableStateFlow<List<Token.Eth>> = MutableStateFlow(emptyList())
    private val ethToken: MutableStateFlow<Token.Eth?> = MutableStateFlow(null)
    private var refreshJob: Job? = null

    val erc20Tokens: StateFlow<List<Token.Eth>> = erC20Tokens


    fun launch(delayInMillis: Long = REFRESH_JOB_DELAY_IN_MILLIS, claimingTokens: List<EthereumClaimToken>) {
        refreshJob?.cancel()
        refreshJob = launch {
            try {
                delay(getDelayTimeInMillis(delayInMillis))
                async { ethToken.emit(loadWallet()) }
                async { erC20Tokens.emit(loadErc20Tokens(claimingTokens)) }
            } catch (e: CancellationException) {
                Timber.tag(TAG).d(e, "Cancellation of token fetching")
            } catch (e: Throwable) {
                Timber.tag(TAG).d(e, "Error on fetching eth tokens")
            }
        }
    }

    private fun isForceFetchRequired(): Boolean = erC20Tokens.value.isEmpty() || ethToken.value == null

    private fun getDelayTimeInMillis(defaultValue: Long): Long = if (isForceFetchRequired()) 0L else defaultValue

    private suspend fun loadWallet(): Token.Eth {
        val walletBalance = repository.getBalance()
        val ethContractAddress = ERC20Tokens.ETH.contractAddress
        val tokenPrice = priceRepository.getPriceForToken(ethContractAddress)
        val totalInUsd = walletBalance.fromLamports(ERC20Tokens.ETH_DECIMALS).times(tokenPrice)
        return Token.Eth(
            publicKey = ethContractAddress,
            totalInUsd = totalInUsd,
            total = walletBalance.fromLamports(ERC20Tokens.ETH_DECIMALS),
            tokenSymbol = ERC20Tokens.ETH.replaceTokenSymbol.orEmpty(),
            decimals = ERC20Tokens.ETH_DECIMALS,
            mintAddress = ERC20Tokens.ETH.mintAddress,
            tokenName = ERC20Tokens.ETH.replaceTokenName.orEmpty(),
            iconUrl = ERC20Tokens.ETH.tokenIconUrl.orEmpty(),
            rate = tokenPrice
        )
    }

    private suspend fun loadErc20Tokens(claimingTokens: List<EthereumClaimToken>): List<Token.Eth> {
        return repository.loadWalletTokens(claimingTokens)
    }
}
