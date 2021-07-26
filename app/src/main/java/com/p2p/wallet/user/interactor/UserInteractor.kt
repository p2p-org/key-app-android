package com.p2p.wallet.user.interactor

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.p2p.wallet.common.crypto.Base58Utils
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.api.TokenColors
import com.p2p.wallet.main.model.TokenConverter
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.user.local.TokenListResponse
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.io.InputStream
import java.math.BigDecimal

private const val KEY_PHRASES = "KEY_PHRASES"

class UserInteractor(
    private val context: Context,
    private val gson: Gson,
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mainLocalRepository: MainLocalRepository,
    private val tokenProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun createAndSaveAccount(keys: List<String>) {
        val account = userRepository.createAccount(keys)
        tokenProvider.secretKey = account.secretKey
        tokenProvider.publicKey = Base58Utils.encode(account.publicKey.toByteArray())

        sharedPreferences.edit {
            putString(KEY_PHRASES, keys.joinToString(","))
        }
    }

    suspend fun loadTokenPrices(targetCurrency: String) {
        val tokens = TokenColors.getSymbols()
        val prices = userRepository.loadTokensPrices(tokens, targetCurrency)
        userLocalRepository.setTokenPrices(prices)
    }

    fun loadTokensData() {
        val data = try {
            val inputStream: InputStream = context.assets.open("tokenlist.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, charset("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }

        if (!data.isNullOrEmpty()) {
            val tokenList = gson.fromJson(data, TokenListResponse::class.java)
            val mappedData = tokenList.tokens.map { TokenConverter.fromNetwork(it) }
            userLocalRepository.setTokenData(mappedData)
        }
    }

    suspend fun loadTokens() {
        val newTokens = userRepository.loadTokens()
        mainLocalRepository.updateTokens(newTokens)
    }

    fun getTokensFlow(): Flow<List<Token>> =
        mainLocalRepository.getTokensFlow()

    suspend fun getTokens(): List<Token> =
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

    fun getSecretKeys(): List<String> =
        sharedPreferences.getString(KEY_PHRASES, "").orEmpty().split(",")
}