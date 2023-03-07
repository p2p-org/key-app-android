package org.p2p.wallet.infrastructure.swap

import androidx.core.content.edit
import android.content.SharedPreferences
import timber.log.Timber
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterAvailableSwapRoutesMap
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

private const val KEY_TOKEN_A_MINT = "KEY_TOKEN_A_MINT"
private const val KEY_TOKEN_B_MINT = "KEY_TOKEN_B_MINT"
private const val KEY_ROUTES_FETCH_DATE = "KEY_ROUTES_FETCH_DATE"
private const val KEY_ROUTES_MINTS = "KEY_ROUTES_MINTS"

class JupiterSwapStorage(
    private val sharedPreferences: SharedPreferences
) : JupiterSwapStorageContract {
    override var savedTokenAMint: Base58String?
        get() = sharedPreferences.getString(KEY_TOKEN_A_MINT, null)?.let(::Base58String)
        set(value) {
            value?.let(::saveTokenA)
        }

    override var savedTokenBMint: Base58String?
        get() = sharedPreferences.getString(KEY_TOKEN_B_MINT, null)?.let(::Base58String)
        set(value) {
            value?.let(::saveTokenB)
        }

    override var routesFetchDateMillis: Long?
        get() = sharedPreferences.getLong(KEY_ROUTES_FETCH_DATE, 0L).takeIf { it != 0L }
        set(value) {
            value?.let(::saveRoutesFetchDate)
        }

    override var routesMap: JupiterAvailableSwapRoutesMap?
        get() = getRoutesMapFromStorage()
        set(value) {
            value?.let(::saveRoutesMapToStorage)
        }

    private fun saveTokenA(mintAddress: Base58String) {
        sharedPreferences.edit { putString(KEY_TOKEN_A_MINT, mintAddress.base58Value) }
    }

    private fun saveTokenB(mintAddress: Base58String) {
        sharedPreferences.edit { putString(KEY_TOKEN_B_MINT, mintAddress.base58Value) }
    }

    private fun saveRoutesFetchDate(dateMillis: Long) {
        sharedPreferences.edit {
            putLong(KEY_ROUTES_FETCH_DATE, dateMillis)
        }
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
    private fun saveRoutesMapToStorage(routesMap: JupiterAvailableSwapRoutesMap) {
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
}
