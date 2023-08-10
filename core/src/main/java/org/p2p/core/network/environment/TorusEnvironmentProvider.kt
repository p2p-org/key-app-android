package org.p2p.core.network.environment

import android.content.Context
import timber.log.Timber
import org.p2p.core.BuildConfig
import org.p2p.core.R
import org.p2p.core.network.NetworkServicesUrlStorage
import org.p2p.core.utils.getStringResourceByName

class TorusEnvironmentProvider(
    private val context: Context,
    private val storage: NetworkServicesUrlStorage,
) {

    private companion object {
        private const val KEY_TORUS_BASE_URL = "KEY_TORUS_BASE_URL"
        private const val KEY_TORUS_BASE_VERIFIER = "KEY_TORUS_BASE_VERIFIER"
        private const val KEY_TORUS_BASE_SUB_VERIFIER = "KEY_TORUS_BASE_SUB_VERIFIER"
    }

    fun loadTorusEnvironment(): TorusEnvironment {
        val url = storage.getString(
            KEY_TORUS_BASE_URL,
            context.getString(R.string.torusBaseUrl)
        ).orEmpty()
        val verifier = storage.getString(
            KEY_TORUS_BASE_VERIFIER,
            context.getString(R.string.torusVerifier)
        ).orEmpty()
        Timber.d("Torus verifier: $verifier")

        val subVerifier = storage.getString(
            KEY_TORUS_BASE_SUB_VERIFIER,
            context.getStringResourceByName("torusSubVerifier")
        ).orEmpty()

        if (!BuildConfig.DEBUG && subVerifier.isBlank()) {
            Timber.e(IllegalArgumentException("torusSubVerifier is missing for release builds!"))
        }

        val torusEnvironment = TorusEnvironment(
            baseUrl = url,
            verifier = verifier,
            subVerifier = subVerifier,
            torusNetwork = context.getString(R.string.torusNetwork),
            torusLogLevel = context.getString(R.string.torusLogLevel)
        )

        Timber.i("Torus environment init: $torusEnvironment")
        return torusEnvironment
    }

    fun saveTorusEnvironment(newUrl: String?, newVerifier: String?, newSubVerifier: String?) {
        storage.edit {
            newUrl?.let {
                putString(KEY_TORUS_BASE_URL, it)
            }
            newVerifier?.let {
                putString(KEY_TORUS_BASE_VERIFIER, it)
            }
            newSubVerifier?.let {
                putString(KEY_TORUS_BASE_SUB_VERIFIER, it)
            } ?: run {
                remove(KEY_TORUS_BASE_SUB_VERIFIER)
            }
        }
        Timber.i("Torus environment changed and saved: $newUrl; $newVerifier; $newSubVerifier")
    }
}
