package org.p2p.ethereumkit.external.repository

import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.orZero
import org.p2p.ethereumkit.external.balance.EthereumTokensRepository
import org.p2p.ethereumkit.external.core.CoroutineDispatchers
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.ethereumkit.external.model.EthTokenConverter
import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import org.p2p.ethereumkit.external.model.EthTokenMetadata
import org.p2p.ethereumkit.external.model.mapToTokenMetadata
import org.p2p.ethereumkit.external.price.PriceRepository
import org.p2p.ethereumkit.internal.core.signer.Signer
import org.p2p.ethereumkit.internal.models.Chain
import org.p2p.core.wrapper.eth.EthAddress

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

    override suspend fun getBalance(): BigInteger {
        val publicKey = tokenKeyProvider?.publicKey ?: throwInitError()
        return balanceRepository.getWalletBalance(publicKey)
    }

    override suspend fun loadWalletTokens(): List<Token.Eth> = withContext(dispatchers.io) {

        val walletTokens = loadTokensMetadata().filter { it.balance.isMoreThan(MINIMAL_DUST) }

        val tokensPrice = getPriceForTokens(tokenAddresses = walletTokens.map { it.contractAddress.toString() })
        tokensPrice.forEach { (address, price) ->
            walletTokens.find { it.contractAddress.hex == address }?.price = price
        }
        return@withContext (listOf(getWalletMetadata()) + walletTokens).map { EthTokenConverter.ethMetadataToToken(it) }
    }

    override suspend fun getAddress(): EthAddress {
        return tokenKeyProvider?.publicKey ?: throwInitError()
    }

    override suspend fun signTransaction() {
        val result = Signer.getInstance(tokenKeyProvider!!.privateKey, Chain.Ethereum)

        println(result)
    }

    private suspend fun getPriceForTokens(tokenAddresses: List<String>): Map<String, BigDecimal> {
        return priceRepository.getTokenPrice(tokenAddresses = tokenAddresses)
            .mapValues { it.value.priceInUsd }
    }

    private suspend fun loadTokensMetadata(): List<EthTokenMetadata> = withContext(dispatchers.io) {
        val publicKey = tokenKeyProvider?.publicKey ?: error("")
        val tokenAddresses = ERC20Tokens.values().map { EthAddress(it.contractAddress) }
        return@withContext balanceRepository.getTokenBalances(address = publicKey, tokenAddresses = tokenAddresses)
            .balances
            .map { token ->
                async {
                    val metadata = balanceRepository.getTokenMetadata(token.contractAddress)
                    val erc20Token = ERC20Tokens.findToken(token.contractAddress)
                    mapToTokenMetadata(balanceResponse = token, metadata = metadata, erc20Token = erc20Token)
                }
            }
            .awaitAll()
    }

    //Temporary solution of creating ETH wallet
    private suspend fun getWalletMetadata(): EthTokenMetadata {
        val erc20TokenAddress = ERC20Tokens.ETH.contractAddress.lowercase()
        val contractAddress = tokenKeyProvider?.publicKey ?: throwInitError()
        val balance = getBalance()
        val price = priceRepository.getTokenPrice(listOf(erc20TokenAddress))
        return EthTokenMetadata(
            contractAddress = contractAddress,
            mintAddress = ERC20Tokens.ETH.mintAddress,
            balance = balance,
            decimals = 18,
            logoUrl = ERC20Tokens.ETH.tokenIconUrl.orEmpty(),
            tokenName = ERC20Tokens.ETH.replaceTokenName.orEmpty(),
            symbol = ERC20Tokens.ETH.replaceTokenSymbol.orEmpty(),
            price = price[erc20TokenAddress]?.priceInUsd.orZero()
        )
    }

    private fun throwInitError(): Nothing =
        error("You must call EthereumKitRepostory.init() method, before interact with this repository")
}

val bundle =
    "0xf8aa8085174876e800830144d9943ee18b2214aff97000d974cf647e7c347e8fa58580b8849981509f00000000000000000000000000000000000000000000000000000000000000014eeaa58f326f13b7f3df4dd50f4f093cf6040c0cbae465546b70fcdaf7f661ae000000000000000000000000000000000000000000000000000446db017ce94c0000000000000000000000000000000000000000000000000000000000000c94018080"
