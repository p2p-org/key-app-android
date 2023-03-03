package org.p2p.ethereumkit.internal.api.storage

import androidx.room.*
import org.p2p.ethereumkit.internal.api.models.AccountState

@Dao
interface AccountStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(accountState: AccountState)

    @Query("SELECT * FROM AccountState LIMIT 1")
    fun getAccountState(): AccountState?

}
