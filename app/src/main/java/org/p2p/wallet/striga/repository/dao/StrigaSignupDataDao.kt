package org.p2p.wallet.striga.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StrigaSignupDataDao {
    @Query("SELECT COUNT(*) FROM striga_signup_data WHERE owner = :userPublicKey ")
    suspend fun countSignupDataForUser(userPublicKey: String): Int

    @Query("SELECT * FROM striga_signup_data WHERE owner = :userPublicKey")
    suspend fun getSignupDataForUser(userPublicKey: String): List<StrigaSignupDataEntity>

    @Query("SELECT * from striga_signup_data")
    fun observeAllSignupData(): Flow<List<StrigaSignupDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateOrInsertData(data: StrigaSignupDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateOrInsertData(data: List<StrigaSignupDataEntity>)

    @Query("DELETE FROM striga_signup_data WHERE owner = :userPublicKey")
    suspend fun deleteDataForUser(userPublicKey: String)
}
