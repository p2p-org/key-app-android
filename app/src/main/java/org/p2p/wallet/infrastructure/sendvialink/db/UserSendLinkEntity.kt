package org.p2p.wallet.infrastructure.sendvialink.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import org.p2p.core.utils.MillisSinceEpoch
import org.p2p.wallet.utils.Base58String

@Entity(tableName = "user_send_links")
class UserSendLinkEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ordinal")
    val ordinal: Int = 0,
    @ColumnInfo(name = "link")
    val link: String,
    @ColumnInfo(name = "amount")
    val amount: BigDecimal,
    @ColumnInfo(name = "token_mint")
    val tokenMint: Base58String,
    @ColumnInfo(name = "date_created_in_epoch")
    val dateCreated: MillisSinceEpoch,
    @ColumnInfo(name = "owner_address")
    val linkOwnerAddress: Base58String
)
