package com.p2p.wallet.user.interactor

import android.content.Context
import com.google.gson.Gson
import com.p2p.wallet.R
import com.p2p.wallet.main.api.TokenColors
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.user.local.TokenListResponse
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import org.p2p.solanaj.core.PublicKey
import java.math.BigDecimal

private const val CHUNKED_SIZE = 50

class UserInteractor(
    private val context: Context,
    private val gson: Gson,
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mainLocalRepository: MainLocalRepository,
    private val rpcRepository: RpcRepository
) {

    suspend fun loadTokenPrices(targetCurrency: String) {
        val tokens = TokenColors.getSymbols()
        val prices = userRepository.loadTokensPrices(tokens, targetCurrency)
        userLocalRepository.setTokenPrices(prices)
    }

    fun loadAllTokensData() {
        val data = context.resources.openRawResource(R.raw.tokenlist)
            .bufferedReader()
            .use { it.readText() }

        if (data.isNotBlank()) {

            val tokenList = gson.fromJson(data, TokenListResponse::class.java)
            val mappedData = tokenList.tokens
                .chunked(CHUNKED_SIZE)
                .flatMap { chunkedList ->
                    chunkedList.map { TokenConverter.fromNetwork(it) }
                }

            userLocalRepository.setTokenData(mappedData)
        }
    }

    suspend fun loadUserTokensAndUpdateData() {
        val newTokens = userRepository.loadTokens()
        mainLocalRepository.updateTokens(newTokens)
    }

    suspend fun getBalance(address: PublicKey) = rpcRepository.getBalance(address)

    fun getUserTokensFlow(): Flow<List<Token>> =
        mainLocalRepository.getTokensFlow()

    suspend fun getUserTokens(): List<Token> =
        mainLocalRepository.getTokens()

    suspend fun setTokenHidden(mintAddress: String, visibility: String) =
        mainLocalRepository.setTokenHidden(mintAddress, visibility)

    suspend fun getPriceByToken(source: String, destination: String): BigDecimal =
        userRepository.getRate(source, destination)

    suspend fun clearMemoryData() {
        mainLocalRepository.setTokens(emptyList())
    }

    suspend fun findAccountAddress(mintAddress: String): Token? =
        mainLocalRepository.getTokens().firstOrNull {
            it.mintAddress == mintAddress
        }
}