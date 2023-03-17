package org.p2p.ethereumkit.internal.api.storage

import androidx.room.*
import org.p2p.ethereumkit.internal.api.models.LastBlockHeight

@Dao
interface LastBlockHeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(lastBlockHeight: LastBlockHeight)

    @Query("SELECT * FROM LastBlockHeight")
    fun getLastBlockHeight(): LastBlockHeight?

}
