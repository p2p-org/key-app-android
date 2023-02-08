package org.p2p.wallet.infrastructure

import android.content.Context
import android.content.SharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.crypto.keystore.EncoderDecoder
import org.p2p.wallet.common.crypto.keystore.EncoderDecoderMarshmallow
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.common.feature_toggles.remote_config.LocalFeatureToggleStorage
import org.p2p.wallet.datastore.UserDataStoreImpl
import org.p2p.wallet.datastore.UserDataStoreService
import org.p2p.wallet.datastore.preferences.UserPreferencesStore
import org.p2p.wallet.infrastructure.account.AccountStorage
import org.p2p.wallet.infrastructure.account.AccountStorageContract
import org.p2p.wallet.infrastructure.security.SecureStorage
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import timber.log.Timber
import java.security.KeyStore

object StorageModule {
    private fun Scope.androidPreferences(prefsName: String): SharedPreferences {
        return with(androidContext()) {
            getSharedPreferences("$packageName.$prefsName", Context.MODE_PRIVATE)
        }
            .also { Timber.tag("StorageModule").i("$prefsName = $it") }
    }

    fun create() = module {

        single<UserPreferencesStore> {
            val preferences = UserDataStoreService.DATA_STORE_MIGRATION_PREFS_NAMES.map { name ->
                get<SharedPreferences>(named(name))
            }
            val dataStore = UserDataStoreService.create(preferences)

            val dataStoreImpl = UserDataStoreImpl(dataStore)
        }
        // TODO PWN-5418 - extract misc data to separate prefs
        single { androidPreferences("prefs") }

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
            val prefs = androidPreferences("account_prefs")
            AccountStorage(
                keyStoreWrapper = KeyStoreWrapper(
                    encoderDecoder = get(),
                    keyStore = get(),
                    sharedPreferences = prefs
                ),
                dataStore = get(),
                gson = get()
            )
        } bind AccountStorageContract::class

        single {
            LocalFeatureToggleStorage(
                sharedPreferences = androidPreferences(prefsName = "toggle_prefs")
            )
        }

        single { KeyStore.getInstance("AndroidKeyStore") }
        factoryOf(::EncoderDecoderMarshmallow) bind EncoderDecoder::class
    }
}
