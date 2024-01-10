package org.p2p.wallet.infrastructure

import android.content.Context
import android.content.SharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber
import java.security.KeyStore
import org.p2p.core.network.NetworkServicesUrlStorage
import org.p2p.core.network.storage.NetworkEnvironmentPreferenceStorage
import org.p2p.core.network.storage.NetworkEnvironmentStorage
import org.p2p.wallet.common.EncryptedSharedPreferences
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.crypto.keystore.EncoderDecoder
import org.p2p.wallet.common.crypto.keystore.EncoderDecoderMarshmallow
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.common.feature_toggles.remote_config.LocalFeatureToggleStorage
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.common.storage.FileRepository
import org.p2p.wallet.common.storage.FilesDirStorageRepository
import org.p2p.wallet.infrastructure.account.AccountStorage
import org.p2p.wallet.infrastructure.account.AccountStorageContract
import org.p2p.wallet.infrastructure.security.SecureStorage
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorage
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.striga.user.storage.StrigaStorage
import org.p2p.wallet.striga.user.storage.StrigaStorageContract

private const val PREFS_DEFAULT = "prefs"
private const val PREFS_ACCOUNT = "account_prefs"
private const val PREFS_TOGGLE = "toggle_prefs"
private const val PREFS_SWAP = "swap_prefs"
private const val PREFS_STRIGA = "striga_prefs"

object StorageModule {
    private fun Scope.androidPreferences(prefsName: String): SharedPreferences {
        return with(androidContext()) {
            getSharedPreferences("$packageName.$prefsName", Context.MODE_PRIVATE)
        }
            .also { Timber.tag("StorageModule").i("$prefsName = $it") }
    }

    private fun Scope.androidEncryptedPreferences(prefsName: String): EncryptedSharedPreferences {
        val prefs = androidPreferences(prefsName)
        val keyStoreWrapper = KeyStoreWrapper(
            encoderDecoder = get(),
            keyStore = get(),
            sharedPreferences = prefs
        )
        return EncryptedSharedPreferences(
            keyStoreWrapper = keyStoreWrapper,
            sharedPreferences = prefs,
            gson = get()
        )
    }

    private fun Module.initStorages() {
        factory { NetworkEnvironmentPreferenceStorage(preferences = get()) } bind
            NetworkEnvironmentStorage::class
        // TODO PWN-5418 - extract data to separate prefs from org.p2p.wallet.prefs
        factory { SecureStorage(get(named(PREFS_DEFAULT))) } bind
            SecureStorageContract::class
        factory { AccountStorage(get(named(PREFS_ACCOUNT))) } bind
            AccountStorageContract::class
        single { JupiterSwapStorage(androidPreferences(PREFS_SWAP), gson = get()) } bind
            JupiterSwapStorageContract::class
        factory { StrigaStorage(get(named(PREFS_STRIGA))) } bind
            StrigaStorageContract::class

        single { LocalFeatureToggleStorage(androidPreferences(prefsName = PREFS_TOGGLE)) }
        single { InAppFeatureFlags(androidPreferences(prefsName = PREFS_TOGGLE)) }
        single { NetworkServicesUrlStorage(androidPreferences(prefsName = PREFS_TOGGLE)) }
    }

    private fun Module.initPrefs() {
        // TODO PWN-5418 - extract misc data to separate prefs
        single {
            androidPreferences(PREFS_DEFAULT)
        }
        single(named(PREFS_DEFAULT)) {
            androidEncryptedPreferences(PREFS_DEFAULT)
        }
        single(named(PREFS_ACCOUNT)) {
            androidEncryptedPreferences(PREFS_ACCOUNT)
        }
        single(named(PREFS_STRIGA)) {
            androidEncryptedPreferences(PREFS_STRIGA)
        }
    }

    fun create() = module {
        initPrefs()
        initStorages()

        single<KeyStore> { KeyStore.getInstance("AndroidKeyStore") }
        factoryOf(::EncoderDecoderMarshmallow) bind EncoderDecoder::class
        factoryOf(::FileRepository)
        factoryOf(::FilesDirStorageRepository) bind ExternalStorageRepository::class
    }
}
