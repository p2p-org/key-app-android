package org.p2p.wallet.infrastructure.proto.migration

interface PreferencesMigrationContract {

    /**
     * Check every created data store file, for migration complete
     * If PreferenceMigrationDataStore.prefs_name = true
     * We successfully migrated preferences to Proto DataStore API
     * returns [true] if migration was already completed, [false] otherwise
     */
    suspend fun checkForMigration(): Boolean

    suspend fun setup()

    suspend fun migrate(): Boolean
}
