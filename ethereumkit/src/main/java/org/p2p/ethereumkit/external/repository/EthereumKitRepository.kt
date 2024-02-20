package org.p2p.ethereumkit.external.repository

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.TOKEN_SERVICE_NATIVE_ETH_TOKEN
import org.p2p.core.utils.orZero
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.core.wrapper.eth.hexStringToBigInteger
import org.p2p.ethereumkit.external.api.alchemy.response.TokenBalanceResponse
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.ethereumkit.external.model.EthTokenConverter
import org.p2p.ethereumkit.external.model.EthTokenKeyProvider
import org.p2p.ethereumkit.external.model.EthTokenMetadata
import org.p2p.ethereumkit.external.model.EthereumClaimToken
import org.p2p.ethereumkit.external.token.EthereumTokenRepository
import org.p2p.ethereumkit.external.token.EthereumTokensLocalRepository
import org.p2p.ethereumkit.internal.core.TransactionSignerEip1559
import org.p2p.ethereumkit.internal.core.TransactionSignerLegacy
import org.p2p.ethereumkit.internal.core.signer.Signer
import org.p2p.ethereumkit.internal.models.Chain
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.TokenServiceRepository

internal class EthereumKitRepository(
    private val tokensRepository: EthereumTokenRepository,
    private val tokensLocalRepository: EthereumTokensLocalRepository,
    private val tokenServiceRepository: TokenServiceRepository,
    private val converter: EthTokenConverter
) : EthereumRepository {

    private var tokenKeyProvider: EthTokenKeyProvider? = null

    override fun init(seedPhrase: List<String>) {
        tokenKeyProvider = EthTokenKeyProvider(
            publicKey = Signer.address(words = seedPhrase, chain = Chain.Ethereum),
            privateKey = Signer.privateKey(words = seedPhrase, chain = Chain.Ethereum)
        )

        tokenKeyProvider?.publicKey ?: return
    }

    override fun isInitialized(): Boolean {
        return tokenKeyProvider != null
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
        return tokensRepository.getWalletBalance(publicKey)
    }

    override suspend fun loadWalletTokens(claimingTokens: List<EthereumClaimToken>): List<Token.Eth> {
        return try {
            loadTokensMetadata().map { tokenMetadata ->
                var isClaiming = false
                var latestBundleId: String? = null
                var tokenAmount: BigDecimal? = null
                var fiatAmount: BigDecimal? = null

                claimingTokens.filter { claimToken -> isTokenClaiming(tokenMetadata, claimToken) }
                    .onEach { claimToken ->
                        isClaiming = true
                        latestBundleId = claimToken.bundleId
                        tokenAmount = claimToken.tokenAmount
                        fiatAmount = claimToken.fiatAmount
                    }

                converter.ethMetadataToToken(
                    metadata = tokenMetadata,
                    bundleId = latestBundleId,
                    isClaiming = isClaiming,
                    tokenAmount = tokenAmount,
                    fiatAmount = fiatAmount
                )
            }
        } catch (e: Throwable) {
            Timber.e(e, "Error on loading ethereumTokens")
            emptyList()
        }
    }

    override suspend fun cacheWalletTokens(tokens: List<Token.Eth>) {
        tokensLocalRepository.cacheTokens(tokens)
    }

    override suspend fun updateTokensRates(rates: List<TokenServicePrice>) {
        tokensLocalRepository.updateTokensRate(rates)
    }

    private fun isTokenClaiming(tokenMetadata: EthTokenMetadata, claimToken: EthereumClaimToken): Boolean {
        return tokenMetadata.contractAddress == claimToken.contractAddress && claimToken.isClaiming
    }

    override fun getAddress(): EthAddress {
        return tokenKeyProvider?.publicKey ?: throwInitError()
    }

    private suspend fun loadTokensMetadata(): List<EthTokenMetadata> {
        val publicKey = tokenKeyProvider?.publicKey ?: throwInitError()
        // Map erc 20 tokens to hex string addresses list
        val erc20TokensAddresses = ERC20Tokens.valuesWithout(ERC20Tokens.MATIC).map { it.contractAddress }
        // Load balances for ERC 20 tokens

        // Create list with [native] token, to receive tokens metadata, from token service
        val allTokensAddresses = buildList<String> {
            this += TOKEN_SERVICE_NATIVE_ETH_TOKEN
            this += erc20TokensAddresses
        }
        // Fetch tokens metadata
        val tokensMetadata = tokenServiceRepository.fetchMetadataForTokens(
            chain = TokenServiceNetwork.ETHEREUM,
            tokenAddresses = allTokensAddresses
        )

        val nativeEthToken = createNativeEthToken(tokensMetadata)

        val tokensBalances = loadTokenBalances(publicKey, erc20TokensAddresses.map(::EthAddress))
        val erc20TokensMetadata = tokensMetadata.map { metadata ->
            val tokenBalance = tokensBalances
                .firstOrNull { it.contractAddress.hex == metadata.address }
                ?.tokenBalanceHex
                ?.hexStringToBigInteger()
                .orZero()

            converter.toEthTokenMetadata(
                metadata = metadata,
                tokenBalance = tokenBalance,
                ethAddress = metadata.address
            )
        }
        return buildList<EthTokenMetadata?> {
            this += nativeEthToken
            this += erc20TokensMetadata
        }.filterNotNull()
    }

    private suspend fun loadTokenBalances(
        address: EthAddress,
        tokenAddresses: List<EthAddress>,
    ): List<TokenBalanceResponse> {
        return tokensRepository.getTokenBalances(address, tokenAddresses).balances
    }

    override fun getWalletTokensFlow(): Flow<List<Token.Eth>> {
        return tokensLocalRepository.getTokensFlow()
    }

    override fun getWalletTokens(): List<Token.Eth> {
        return tokensLocalRepository.getWalletTokens()
    }

    private fun throwInitError(): Nothing =
        error("You must call EthereumKitRepository.init() method, before interact with this repository")

    private suspend fun createNativeEthToken(
        tokensMetadata: List<TokenServiceMetadata>
    ): EthTokenMetadata? {
        // Create metadata for native ETH token
        val nativeEthMetadata = tokensMetadata.firstOrNull {
            it.address == TOKEN_SERVICE_NATIVE_ETH_TOKEN
        }
        // Fetch balance for native ETH token
        val ethBalance = getBalance()

        return nativeEthMetadata?.let {
            converter.createNativeEthMetadata(
                ethAddress = getAddress().hex,
                metadata = it,
                tokenBalance = ethBalance,
            )
        }
    }
}
