package org.p2p.wallet.infrastructure.swap

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import org.p2p.core.utils.fromJsonReified
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.routes.JupiterAvailableSwapRoutesMap
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance

private const val KEY_TOKEN_A_MINT = "KEY_TOKEN_A_MINT"
private const val KEY_TOKEN_B_MINT = "KEY_TOKEN_B_MINT"
private const val KEY_ROUTES_FETCH_DATE = "KEY_ROUTES_FETCH_DATE"
private const val KEY_ROUTES_MINTS = "KEY_ROUTES_MINTS"
private const val KEY_SWAP_TOKENS = "KEY_SWAP_TOKENS"
private const val KEY_SWAP_TOKENS_FETCH_DATE = "KEY_SWAP_TOKENS_FETCH_DATE"

class JupiterSwapStorage(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : JupiterSwapStorageContract {
    override var savedTokenAMint: Base58String?
        get() = sharedPreferences.getString(KEY_TOKEN_A_MINT, null)?.let(::Base58String)
        set(value) {
            value?.let(::saveTokenA) ?: remove(KEY_TOKEN_A_MINT)
        }

    override var savedTokenBMint: Base58String?
        get() = sharedPreferences.getString(KEY_TOKEN_B_MINT, null)?.let(::Base58String)
        set(value) {
            value?.let(::saveTokenB) ?: remove(KEY_TOKEN_B_MINT)
        }

    override var routesFetchDateMillis: Long?
        get() = sharedPreferences.getLong(KEY_ROUTES_FETCH_DATE, 0L).takeIf { it != 0L }
        set(value) {
            value?.let(::saveRoutesFetchDate) ?: remove(KEY_ROUTES_FETCH_DATE)
        }

    override var routesMap: JupiterAvailableSwapRoutesMap?
        get() = getRoutesMapFromStorage()
        set(value) {
            value?.let(::saveRoutesMap) ?: remove(KEY_ROUTES_MINTS)
        }

    override var swapTokensFetchDateMillis: Long?
        get() = sharedPreferences.getLong(KEY_SWAP_TOKENS_FETCH_DATE, 0L).takeIf { it != 0L }
        set(value) {
            value?.let(::saveSwapTokensFetchDate) ?: remove(KEY_SWAP_TOKENS_FETCH_DATE)
        }

    override var swapTokens: List<JupiterSwapToken>?
        get() = getSwapTokensFromStorage()
        set(value) {
            value?.let(::saveSwapTokens) ?: remove(KEY_SWAP_TOKENS)
        }

    override fun clear() {
        savedTokenAMint = null
        savedTokenBMint = null
        routesFetchDateMillis = null
        routesMap = null
        swapTokensFetchDateMillis = null
        swapTokens = null
    }

    private fun saveTokenA(mintAddress: Base58String) {
        sharedPreferences.edit { putString(KEY_TOKEN_A_MINT, mintAddress.base58Value) }
    }

    private fun saveTokenB(mintAddress: Base58String) {
        sharedPreferences.edit { putString(KEY_TOKEN_B_MINT, mintAddress.base58Value) }
    }

    private fun saveRoutesFetchDate(dateMillis: Long) {
        sharedPreferences.edit { putLong(KEY_ROUTES_FETCH_DATE, dateMillis) }
    }

    private fun saveSwapTokensFetchDate(dateMillis: Long) {
        sharedPreferences.edit { putLong(KEY_SWAP_TOKENS_FETCH_DATE, dateMillis) }
    }

    /**
     * To get this complex object we convert:
     * Map<String, Set<String>> -> Map<Int, List<Int>>
     * if it fails - we return null and refetch routes, that's ok
     */
    private fun getRoutesMapFromStorage(): JupiterAvailableSwapRoutesMap? {
        try {
            val mintAddresses: List<Base58String> =
                sharedPreferences.getStringSet(KEY_ROUTES_MINTS, null)
                    ?.map { it.toBase58Instance() }
                    ?.toList()
                    ?: return null

            val routesKeys: Set<String> = sharedPreferences.all.filterKeys { it.toIntOrNull() != null }.keys
            val allRoutes =
                routesKeys.associateWith { sharedPreferences.getStringSet(it, null)!! }
                    .mapKeys { it.key.toInt() }
                    .mapValues { it.value.map(String::toInt).toList() }

            return JupiterAvailableSwapRoutesMap(mintAddresses, allRoutes)
        } catch (parsingError: Throwable) {
            Timber.e(parsingError, "Failed to parse routes")
            return null
        }
    }

    /**
     * To save this complex object we convert:
     * Map<Int, List<Int>> -> Map<String, Set<String>> where key is also used in shared prefs
     */
    private fun saveRoutesMap(routesMap: JupiterAvailableSwapRoutesMap) {
        val mintAddresses: List<String> = routesMap.tokenMints.map(Base58String::base58Value)
        val routesInPrefsFormat: Map<String, Set<String>> =
            routesMap.allRoutes
                .mapKeys { it.key.toString() }
                .mapValues { it.value.map(Int::toString).toSet() }

        sharedPreferences.edit {
            putStringSet(KEY_ROUTES_MINTS, mintAddresses.toSet())
            routesInPrefsFormat.forEach { (key, value) -> putStringSet(key, value) }
        }
    }

    private fun getSwapTokensFromStorage(): List<JupiterSwapToken>? {
        val tokensAsJson = sharedPreferences.getString(KEY_SWAP_TOKENS, null) ?: return null
        return kotlin.runCatching { gson.fromJsonReified<List<JupiterSwapTokenEntity>>(tokensAsJson)!! }
            .onFailure { Timber.e(it) }
            .map { it.map(JupiterSwapTokenEntity::toDomain) }
            .getOrNull()
    }

    private fun saveSwapTokens(tokens: List<JupiterSwapToken>) {
        val tokensAsJson: String = tokens
            .map(JupiterSwapTokenEntity::fromDomain)
            .let<List<JupiterSwapTokenEntity>, String>(gson::toJson)

        sharedPreferences.edit { putString(KEY_SWAP_TOKENS, tokensAsJson) }
    }

    private fun remove(key: String) {
        sharedPreferences.edit { remove(key) }
    }

    private data class JupiterSwapTokenEntity(
        @SerializedName("token_mint")
        val tokenMint: Base58String,
        @SerializedName("chain_id")
        val chainId: Int,
        @SerializedName("decimals")
        val decimals: Int,
        @SerializedName("coingecko_id")
        val coingeckoId: String?,
        @SerializedName("logo_uri")
        val logoUri: String?,
        @SerializedName("token_name")
        val tokenName: String,
        @SerializedName("token_symbol")
        val tokenSymbol: String,
        @SerializedName("tags")
        val tags: List<String>
    ) {
        companion object {
            fun fromDomain(domain: JupiterSwapToken) = domain.run {
                JupiterSwapTokenEntity(
                    tokenMint = tokenMint,
                    chainId = chainId,
                    decimals = decimals,
                    coingeckoId = coingeckoId,
                    logoUri = logoUri,
                    tokenName = tokenName,
                    tokenSymbol = tokenSymbol,
                    tags = tags
                )
            }
        }

        fun toDomain(): JupiterSwapToken = this.run {
            JupiterSwapToken(
                tokenMint = tokenMint,
                chainId = chainId,
                decimals = decimals,
                coingeckoId = coingeckoId,
                logoUri = logoUri,
                tokenName = tokenName,
                tokenSymbol = tokenSymbol,
                tags = tags
            )
        }
    }
}
