package org.p2p.ethereumkit.external.repository

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.core.utils.isMoreThan
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.api.alchemy.response.TokenBalanceResponse
import org.p2p.ethereumkit.external.balance.EthereumTokensRepository
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.ethereumkit.external.model.EthTokenConverter
import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import org.p2p.ethereumkit.external.model.EthTokenMetadata
import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.external.model.mapToTokenMetadata
import org.p2p.ethereumkit.external.price.PriceRepository
import org.p2p.ethereumkit.internal.core.TransactionSignerEip1559
import org.p2p.ethereumkit.internal.core.TransactionSignerLegacy
import org.p2p.ethereumkit.internal.core.signer.Signer
import org.p2p.ethereumkit.internal.models.Chain
import org.p2p.ethereumkit.internal.models.Signature

private val MINIMAL_DUST = BigInteger("1")

internal class EthereumKitRepository(
    private val balanceRepository: EthereumTokensRepository,
    private val priceRepository: PriceRepository,
    private val dispatchers: CoroutineDispatchers,
) : EthereumRepository {

    private var tokenKeyProvider: EthTokenKeyProvider? = null

    override fun init(seedPhrase: List<String>) {
        tokenKeyProvider = EthTokenKeyProvider(
            publicKey = Signer.address(words = seedPhrase, chain = Chain.Ethereum),
            privateKey = Signer.privateKey(words = seedPhrase, chain = Chain.Ethereum)
        )
    }

    override fun getPrivateKey(): BigInteger {
        return tokenKeyProvider?.privateKey ?: throwInitError()
    }

    override fun signTransaction(transaction: HexString): Signature {
        val privateKey = tokenKeyProvider?.privateKey ?: throwInitError()
        val signer = TransactionSignerEip1559(privateKey = privateKey)
        return signer.sign(transaction)
    }

    override fun signTransactionLegacy(transaction: HexString): Signature {
        val privateKey = tokenKeyProvider?.privateKey ?: throwInitError()
        val signer = TransactionSignerLegacy(
            privateKey = privateKey,
            chainId = Chain.Ethereum.id
        )
        return signer.signatureLegacy(transaction)
    }

    override suspend fun getBalance(): BigInteger {
        val publicKey = tokenKeyProvider?.publicKey ?: throwInitError()
        return balanceRepository.getWalletBalance(publicKey)
    }

    override suspend fun loadWalletTokens(claimingTokens: List<EthereumClaimToken>): List<Token.Eth> =
        withContext(dispatchers.io) {
            try {
                val tokensMetadata = loadTokensMetadata()

                getPriceForTokens(tokensMetadata.map { it.contractAddress.toString() })
                    .onEach { (address, price) ->
                        tokensMetadata.find { it.contractAddress.hex == address }?.price = price
                    }

                tokensMetadata.filter { metadata ->
                    val tokenBundle = claimingTokens.firstOrNull { metadata.contractAddress == it.contractAddress }
                    val isClaimInProgress = tokenBundle != null && tokenBundle.isClaiming
                    metadata.balance.isMoreThan(MINIMAL_DUST) || isClaimInProgress
                }.map { EthTokenConverter.ethMetadataToToken(it) }

            } catch (cancellation: CancellationException) {
                Timber.i(cancellation)
                emptyList()
            } catch (e: Throwable) {
                Timber.e(e, "Error on loading ethereumTokens")
                emptyList()
            }
        }

    override suspend fun getAddress(): EthAddress {
        return tokenKeyProvider?.publicKey ?: throwInitError()
    }

    private suspend fun getPriceForTokens(tokenAddresses: List<String>): Map<String, BigDecimal> {
        return kotlin.runCatching { priceRepository.getPriceForTokens(tokenAddresses) }
            .getOrDefault(emptyMap())
    }

    private suspend fun loadTokensMetadata(): List<EthTokenMetadata> = withContext(dispatchers.io) {
        val publicKey = tokenKeyProvider?.publicKey ?: throwInitError()
        val tokenAddresses = ERC20Tokens.values().map { EthAddress(it.contractAddress) }

        loadTokenBalances(publicKey, tokenAddresses).map { tokenBalance ->
            getMetadataAsync(
                tokenBalance = tokenBalance,
                contractAddress = tokenBalance.contractAddress
            )
        }.awaitAll()
    }

    private suspend fun loadTokenBalances(
        address: EthAddress,
        tokenAddresses: List<EthAddress>,
    ): List<TokenBalanceResponse> {
        return balanceRepository.getTokenBalances(address, tokenAddresses).balances
    }

    private suspend fun getMetadataAsync(tokenBalance: TokenBalanceResponse, contractAddress: EthAddress) =
        coroutineScope {
            async {
                val metadata = balanceRepository.getTokenMetadata(contractAddress)
                val erc20Token = ERC20Tokens.findToken(contractAddress)
                mapToTokenMetadata(tokenBalance, metadata, erc20Token)
            }
        }

    private fun throwInitError(): Nothing =
        error("You must call EthereumKitRepository.init() method, before interact with this repository")
}
