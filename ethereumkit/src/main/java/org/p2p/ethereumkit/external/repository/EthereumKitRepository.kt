package org.p2p.ethereumkit.external.repository

import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.ethereumkit.external.balance.EthereumTokensRepository
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import org.p2p.ethereumkit.external.model.EthTokenMetadata
import org.p2p.ethereumkit.external.model.mapToTokenMetadata
import org.p2p.ethereumkit.external.price.PriceRepository
import org.p2p.ethereumkit.internal.core.signer.Signer
import org.p2p.ethereumkit.internal.models.Chain
import org.p2p.ethereumkit.internal.models.EthAddress

internal class EthereumKitRepository(
    private val balanceRepository: EthereumTokensRepository,
    private val priceRepository: PriceRepository,
    private val dispatchers: CoroutineDispatchers
) : EthereumRepository {

    private var tokenKeyProvider: EthTokenKeyProvider? = null

    override fun init(seedPhrase: List<String>) {
        tokenKeyProvider = EthTokenKeyProvider(
            publicKey = Signer.address(words = seedPhrase, chain = Chain.Ethereum),
            privateKey = Signer.privateKey(words = seedPhrase, chain = Chain.Ethereum)
        )
    }

    override suspend fun getBalance(): BigInteger {
        val publicKey = tokenKeyProvider?.publicKey ?: error("init function wasn't called")
        return balanceRepository.getWalletBalance(publicKey)
    }

    override suspend fun loadWalletTokens(): List<EthTokenMetadata> = withContext(dispatchers.io) {
        val walletTokens = loadTokensMetadata()
        val tokensPrice = getPriceForTokens(tokenAddresses = walletTokens.map { it.contractAddress.toString() })
        tokensPrice.forEach { (address, price) ->
            walletTokens.find { it.contractAddress.hex == address }?.price = price
        }
        return@withContext walletTokens
    }

    private suspend fun getPriceForTokens(tokenAddresses: List<String>): Map<String, BigDecimal> {
        return priceRepository.getTokenPrice(tokenAddresses = tokenAddresses)
            .mapValues { it.value.priceInUsd }
    }

    private suspend fun loadTokensMetadata(): List<EthTokenMetadata> = withContext(dispatchers.io) {
        val publicKey = tokenKeyProvider?.publicKey ?: error("init function wasn't called")
        val tokenAddresses = ERC20Tokens.values().map { EthAddress(it.contractAddress) }
        return@withContext balanceRepository.getTokenBalances(address = publicKey, tokenAddresses = tokenAddresses)
            .balances
            .map { token ->
                async {
                    val metadata = balanceRepository.getTokenMetadata(token.contractAddress)
                    val mintAddress = ERC20Tokens.findToken(token.contractAddress).mintAddress
                    mapToTokenMetadata(balanceResponse = token, metadata = metadata, mintAddress = mintAddress)
                }
            }
            .awaitAll()
    }
}
