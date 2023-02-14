package org.p2p.wallet.infrastructure.proto.migration

import android.content.SharedPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.p2p.wallet.AuthDataStore
import org.p2p.wallet.PreferencesMigrationDataStore
import org.p2p.wallet.infrastructure.proto.DataStoreModelContract

class AuthPreferencesMigrationManager(
    private val authDataStore: DataStoreModelContract<AuthDataStore>,
    private val migrationDataStore: DataStoreModelContract<PreferencesMigrationDataStore>,
    private val authPreferences: SharedPreferences
) : PreferencesMigrationContract {

    override suspend fun checkForMigration(): Boolean {
        return migrationDataStore.getDataStoreModel().data.map { it.isAuthPreferencesMigrated }.first()
    }

    override suspend fun setup() {}

    override suspend fun migrate(): Boolean {
        TODO()
    }

    private enum class PreferenceAuthKeys(val prefsName: String) {
        KEY_PIN_CODE_HASH("KEY_PIN_CODE_HASH"),
        KEY_PIN_CODE_BIOMETRIC_HASH("KEY_PIN_CODE_BIOMETRIC_HASH"),
        KEY_PIN_CODE_SALT("KEY_PIN_CODE_SALT"),
        KEY_STUB_PUBLIC_KEY("KEY_STUB_PUBLIC_KEY"),
        KEY_USE_STUB_PUBLIC_KEY("KEY_USE_STUB_PUBLIC_KEY"),
        KEY_ONBOARDING_METADATA("KEY_ONBOARDING_METADATA"),
        KEY_SEED_PHRASE("KEY_SEED_PHRASE"),
        KEY_SEED_PHRASE_PROVIDER("KEY_SEED_PHRASE_PROVIDER"),
        KEY_IS_SELL_WARNING_SHOWED("KEY_IS_SELL_WARNING_SHOWED")
    }
}
