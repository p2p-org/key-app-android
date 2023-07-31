package org.p2p.wallet.user.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import timber.log.Timber
import java.util.Date
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.solanaj.core.PublicKey
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.repository.RecipientsLocalRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserTokensLocalRepository
import org.p2p.wallet.utils.emptyString

private const val KEY_HIDDEN_TOKENS_VISIBILITY = "KEY_HIDDEN_TOKENS_VISIBILITY"
val TOKEN_SYMBOLS_VALID_FOR_BUY: List<String> = listOf(Constants.USDC_SYMBOL, Constants.SOL_SYMBOL)

class UserInteractor(
    private val userLocalRepository: UserLocalRepository,
    private val userTokensRepository: UserTokensLocalRepository,
    private val userTokensInteractor: UserTokensInteractor,
    private val recipientsLocalRepository: RecipientsLocalRepository,
    private val rpcRepository: RpcBalanceRepository,
    private val sharedPreferences: SharedPreferences,
    private val tokenServiceRepository: TokenServiceRepository,
) {

    suspend fun findTokenData(mintAddress: String): Token? {
        val tokenData = userLocalRepository.findTokenData(mintAddress)
        val price = tokenData?.let {
            tokenServiceRepository.findTokenPriceByAddress(
                tokenAddress = it.mintAddress,
                networkChain = TokenServiceNetwork.SOLANA
            )
        }
        return tokenData?.let { TokenConverter.fromNetwork(it, price) }
    }

    suspend fun getSingleTokenForBuy(availableTokensSymbols: List<String> = TOKEN_SYMBOLS_VALID_FOR_BUY): Token? =
        getTokensForBuy(availableTokensSymbols).firstOrNull()

    suspend fun getTokensForBuy(
        availableTokensSymbols: List<String> = TOKEN_SYMBOLS_VALID_FOR_BUY
    ): List<Token> {
        val userTokens = userTokensRepository.getUserTokens()
        val allTokens = availableTokensSymbols.mapNotNull { tokenSymbol ->
            val userToken = userTokens.find { it.tokenSymbol == tokenSymbol }
            userToken ?: findTokenDataBySymbol(tokenSymbol)
        }

        if (allTokens.isEmpty()) {
            Timber.e(
                IllegalStateException("No tokens to buy! All tokens: ${userTokens.map(Token.Active::tokenSymbol)}")
            )
        }
        return allTokens
    }

    suspend fun getBalance(address: PublicKey): Long = rpcRepository.getBalance(address)

    fun fetchTokens(searchText: String = emptyString(), count: Int, refresh: Boolean) {
        userLocalRepository.fetchTokens(searchText, count, refresh)
    }

    fun getTokenListFlow() = userLocalRepository.getTokenListFlow()

    fun getHiddenTokensVisibility(): Boolean {
        return sharedPreferences.getBoolean(KEY_HIDDEN_TOKENS_VISIBILITY, false)
    }

    fun setHiddenTokensVisibility(visible: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_HIDDEN_TOKENS_VISIBILITY, visible)
        }
    }

    suspend fun getNonZeroUserTokens(): List<Token.Active> =
        userTokensRepository.getUserTokens()
            .filterNot { it.isZero }

    suspend fun setTokenHidden(mintAddress: String, visibility: String) =
        userTokensInteractor.setTokenHidden(mintAddress, visibility)

    suspend fun hasAccount(address: String): Boolean {
        val userTokens = userTokensInteractor.getUserTokens()
        return userTokens.any { it.publicKey == address }
    }

    suspend fun findMultipleTokenData(tokenSymbols: List<String>): List<Token.Other> =
        tokenSymbols.mapNotNull { findTokenDataBySymbol(it) }

    private suspend fun findTokenDataBySymbol(symbol: String): Token.Other? {
        val tokenData = userLocalRepository.findTokenDataBySymbol(symbol)
        val price = tokenData?.let {
            tokenServiceRepository.findTokenPriceByAddress(
                tokenAddress = it.mintAddress,
                networkChain = TokenServiceNetwork.SOLANA
            )
        }
        return tokenData?.let { TokenConverter.fromNetwork(it, price) }
    }

    suspend fun findTokenDataByAddress(mintAddress: String): Token? = userLocalRepository.findTokenByMint(mintAddress)

    suspend fun addRecipient(searchResult: SearchResult, date: Date) {
        recipientsLocalRepository.addRecipient(searchResult, date)
    }

    suspend fun getRecipients(): List<SearchResult> = recipientsLocalRepository.getRecipients()

    private fun List<Token.Active>.filterNotSol(): List<Token.Active> =
        filterNot { it.mintAddress == Constants.WRAPPED_SOL_MINT }
}
