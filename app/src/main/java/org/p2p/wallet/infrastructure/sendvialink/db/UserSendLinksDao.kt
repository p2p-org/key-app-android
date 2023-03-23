package org.p2p.wallet.infrastructure.sendvialink.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserSendLinksDao {
    @Query("SELECT * FROM user_send_links WHERE owner_address = :userAddress ORDER BY date_created_in_epoch DESC")
    suspend fun getLinks(userAddress: String): List<UserSendLinkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLink(userLink: UserSendLinkEntity)
}
