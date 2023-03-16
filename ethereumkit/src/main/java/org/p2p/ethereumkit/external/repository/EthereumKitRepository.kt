package org.p2p.ethereumkit.external.repository

import org.web3j.crypto.TransactionDecoder
import org.web3j.crypto.TransactionEncoder
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.orZero
import org.p2p.core.wrapper.HexString
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
import org.p2p.ethereumkit.internal.core.TransactionSignerLegacy
import org.p2p.ethereumkit.internal.core.toHexString
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

    override fun signTransaction(transaction: HexString): Pair<Signature, String> {
        val decodedTransaction = TransactionDecoder.decode(transaction.rawValue)
        val signer = TransactionSignerLegacy(
            privateKey = tokenKeyProvider?.privateKey ?: throwInitError(),
            chainId = Chain.Ethereum.id
        )
        val hex = TransactionEncoder.encode(decodedTransaction, 1L)
        val signature = signer.signatureLegacy(decodedTransaction)
        return signature to hex.toHexString()
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
