package com.p2p.wallet.user.repository

import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.infrastructure.network.provider.PublicKeyProvider
import com.p2p.wallet.main.api.CompareApi
import com.p2p.wallet.main.model.TokenPrice
import com.p2p.wallet.user.model.UserConverter
import com.p2p.wallet.utils.WalletDataConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.rpc.RpcClient
import java.math.BigDecimal

interface UserRepository {
    suspend fun createAccount(keys: List<String>): Account
    suspend fun loadSolBalance(): Long
    suspend fun loadTokensPrices(tokens: List<String>, targetCurrency: String): List<TokenPrice>
    suspend fun loadTokens(targetCurrency: String): List<Token>
    suspend fun loadDecimals(publicKey: String): Int
}

class UserRepositoryImpl(
    private val client: RpcClient,
    private val compareApi: CompareApi,
    private val tokenProvider: PublicKeyProvider
) : UserRepository {

    override suspend fun createAccount(keys: List<String>): Account = withContext(Dispatchers.IO) {
        Account.fromMnemonic(keys, "")
    }

    override suspend fun loadTokensPrices(tokens: List<String>, targetCurrency: String): List<TokenPrice> {
        val response = compareApi.getTokensPrice(tokens.joinToString(","), targetCurrency)
        return emptyList() // todo: the purpose of this is to cache tokens price to quickly update data from memory
    }

    /**
     * Temporary passing keys here, but we should inject key provider in upper level, for example in [RpcApi]
     **/
    override suspend fun loadSolBalance(): Long = withContext(Dispatchers.IO) {
        client.api.getBalance(PublicKey(tokenProvider.publicKey))
    }

    override suspend fun loadTokens(targetCurrency: String): List<Token> = withContext(Dispatchers.IO) {
        val response = client.api.getProgramAccounts(
            PublicKey(tokenProvider.programPublicKey),
            32,
            tokenProvider.publicKey
        )

        val tokenAccounts = response.map { UserConverter.fromNetwork(it) }
        val solBalance = loadSolBalance()
        val wallets = WalletDataConst.getWalletConstList()

        val result = wallets
            .mapNotNull { wallet ->
                val account = tokenAccounts.find { it.mintAddress == wallet.mint } ?: return@mapNotNull null
                val decimals = loadDecimals(account.mintAddress)
                val exchangeRate = getPriceByToken(wallet.tokenSymbol, targetCurrency)

                Token(
                    tokenSymbol = wallet.tokenSymbol,
                    tokenName = wallet.tokenName,
                    iconUrl = wallet.icon,
                    depositAddress = account.depositAddress,
                    mintAddress = account.mintAddress,
                    price = account.getFormattedPrice(exchangeRate, decimals),
                    total = account.getAmount(decimals),
                    decimals = decimals,
                    walletBinds = if (wallet.isUS()) 1.0 else 0.0,
                    color = wallet.color
                )
            }
            .toMutableList()
            .apply {
                sortByDescending { it.total }
            }

        val sol = Token.getSOL(tokenProvider.publicKey, solBalance)
        val solRate = getPriceByToken(sol.tokenSymbol, targetCurrency)
        result.add(0, sol.copy(price = sol.total.times(solRate)))
        return@withContext result
    }

    override suspend fun loadDecimals(publicKey: String): Int = withContext(Dispatchers.IO) {
        val response = client.api.getAccountInfo(PublicKey(publicKey))
        UserConverter.fromNetwork(response.value.data ?: emptyList())
    }

    private suspend fun getPriceByToken(fromToken: String, toToken: String): BigDecimal =
        BigDecimal(compareApi.getUSPrice(fromToken, toToken).value)
}