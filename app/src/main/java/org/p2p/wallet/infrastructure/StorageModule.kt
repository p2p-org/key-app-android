package org.p2p.wallet.infrastructure

import android.content.Context
import android.content.SharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber
import java.security.KeyStore
import org.p2p.wallet.common.crypto.keystore.EncoderDecoder
import org.p2p.wallet.common.crypto.keystore.EncoderDecoderMarshmallow
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.common.feature_toggles.remote_config.LocalFeatureToggleStorage
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.common.storage.FileRepository
import org.p2p.wallet.infrastructure.account.AccountStorage
import org.p2p.wallet.infrastructure.account.AccountStorageContract
import org.p2p.wallet.infrastructure.security.SecureStorage
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorage
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract

private const val PREFS_DEFAULT = "prefs"
private const val PREFS_ACCOUNT = "account_prefs"
private const val PREFS_TOGGLE = "toggle_prefs"
private const val PREFS_SWAP = "swap_prefs"

object StorageModule {
    private fun Scope.androidPreferences(prefsName: String): SharedPreferences {
        return with(androidContext()) {
            getSharedPreferences("$packageName.$prefsName", Context.MODE_PRIVATE)
        }
            .also { Timber.tag("StorageModule").i("$prefsName = $it") }
    }

    fun create() = module {
        // TODO PWN-5418 - extract misc data to separate prefs
        single { androidPreferences(PREFS_DEFAULT) }

        // TODO PWN-5418 - extract data to separate prefs from org.p2p.wallet.prefs
        factory {
            val sharedPreferences: SharedPreferences = get()

            SecureStorage(
                KeyStoreWrapper(
                    encoderDecoder = get(),
                    keyStore = get(),
                    sharedPreferences = sharedPreferences
                ),
                sharedPreferences = sharedPreferences,
                gson = get()
            )
        } bind SecureStorageContract::class
        factory {
            val prefs = androidPreferences(PREFS_ACCOUNT)
            AccountStorage(
                keyStoreWrapper = KeyStoreWrapper(
                    encoderDecoder = get(),
                    keyStore = get(),
                    sharedPreferences = prefs
                ),
                sharedPreferences = prefs,
                gson = get()
            )
        } bind AccountStorageContract::class

        single {
            LocalFeatureToggleStorage(
                sharedPreferences = androidPreferences(prefsName = PREFS_TOGGLE)
            )
        }

        factory {
            val prefs = androidPreferences(PREFS_SWAP)
            JupiterSwapStorage(prefs, gson = get())
        } bind JupiterSwapStorageContract::class

        single { KeyStore.getInstance("AndroidKeyStore") }
        factoryOf(::EncoderDecoderMarshmallow) bind EncoderDecoder::class
        factoryOf(::FileRepository)
        factoryOf(::ExternalStorageRepository)
    }
}
