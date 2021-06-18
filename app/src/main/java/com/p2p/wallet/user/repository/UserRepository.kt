package com.p2p.wallet.user.repository

import com.p2p.wallet.amount.fromLamports
import com.p2p.wallet.amount.scalePrice
import com.p2p.wallet.amount.toPowerValue
import com.p2p.wallet.amount.valueOrZero
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.api.BonfidaApi
import com.p2p.wallet.main.api.CompareApi
import com.p2p.wallet.main.api.TokenColors
import com.p2p.wallet.main.model.TokenPrice
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.TokenVisibility
import com.p2p.wallet.user.model.TokenBid
import com.p2p.wallet.user.model.UserConverter
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.rpc.RpcClient
import java.math.BigDecimal

interface UserRepository {
    suspend fun createAccount(keys: List<String>): Account
    suspend fun loadSolBalance(): Long
    suspend fun loadTokensPrices(tokens: List<String>, targetCurrency: String): List<TokenPrice>
    suspend fun loadTokenBids(tokens: List<String>): List<TokenBid>
    suspend fun loadTokens(): List<Token>
    suspend fun getRate(source: String, destination: String): BigDecimal
}

class UserRepositoryImpl(
    private val client: RpcClient,
    private val compareApi: CompareApi,
    private val bonfidaApi: BonfidaApi,
    private val tokenProvider: TokenKeyProvider,
    private val userLocalRepository: UserLocalRepository
) : UserRepository {

    override suspend fun createAccount(keys: List<String>): Account = withContext(Dispatchers.IO) {
        Account.fromMnemonic(keys, "")
    }

    override suspend fun loadTokensPrices(tokens: List<String>, targetCurrency: String): List<TokenPrice> {
        val response = compareApi.getMultiPrice(tokens.joinToString(","), targetCurrency)
        return tokens.map { UserConverter.fromNetwork(it, response) }
    }

    /**
     * Temporary passing keys here, but we should inject key provider in upper level, for example in [RpcApi]
     **/
    override suspend fun loadSolBalance(): Long = withContext(Dispatchers.IO) {
        client.api.getBalance(tokenProvider.publicKey.toPublicKey())
    }

    override suspend fun loadTokens(): List<Token> = withContext(Dispatchers.IO) {
        val response = TokenTransaction.getTokenAccountsByOwner(client, tokenProvider.publicKey.toPublicKey())

        val result = response.accounts
            .mapNotNull {
                val mintAddress = it.account.data.parsed.info.mint
                val token = userLocalRepository.getDecimalsByToken(mintAddress) ?: return@mapNotNull null
                val total = it.account.data.parsed.info.tokenAmount.amount.toBigInteger()
                val price = userLocalRepository.getPriceByToken(token.symbol)
                Token(
                    publicKey = it.pubkey,
                    mintAddress = mintAddress,
                    tokenSymbol = token.symbol,
                    decimals = token.decimals,
                    tokenName = token.name,
                    logoUrl = token.iconUrl,
                    price = total.fromLamports(token.decimals).times(price.price),
                    total = BigDecimal(total).divide(token.decimals.toPowerValue()),
                    color = TokenColors.findColorBySymbol(token.symbol),
                    walletBinds = userLocalRepository.getBidByToken(token.symbol).bid,
                    usdRate = price.price,
                    visibility = TokenVisibility.DEFAULT
                )
            }
            .toMutableList()
            .apply {
                sortByDescending { it.total }
            }

        val solBalance = loadSolBalance()

        /*
         * Assuming that SOL is our default token
         * */
        val sol = Token.getSOL(tokenProvider.publicKey, solBalance)
        val solPrice = userLocalRepository.getPriceByToken(sol.tokenSymbol)
        val solBid = userLocalRepository.getBidByToken(sol.tokenSymbol)
        val solExchangeRate = solPrice.getFormattedPrice()
        val element = sol.copy(
            price = sol.total.multiply(solExchangeRate),
            usdRate = solExchangeRate.scalePrice(),
            walletBinds = solBid.bid
        )
        result.add(0, element)
        return@withContext result
    }

    override suspend fun loadTokenBids(tokens: List<String>): List<TokenBid> =
        coroutineScope {
            tokens
                .map { symbol ->
                    async {
                        try {
                            val response = bonfidaApi.getOrderBooks(symbol.toOrderBookValue())
                            val bid = response.data.bids.firstOrNull()?.price.valueOrZero()
                            TokenBid(symbol, bid)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            TokenBid(symbol, BigDecimal.ZERO)
                        }
                    }
                }
                .awaitAll()
        }

    override suspend fun getRate(source: String, destination: String): BigDecimal {
        val data = compareApi.getPrice(source, destination)
        return UserConverter.fromNetwork(destination, data).getFormattedPrice()
    }

    private fun String.toOrderBookValue(): String = "${this}USDT"
}