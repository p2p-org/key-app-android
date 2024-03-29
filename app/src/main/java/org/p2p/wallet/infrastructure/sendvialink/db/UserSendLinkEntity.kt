package org.p2p.wallet.infrastructure.sendvialink.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.math.BigDecimal
import java.util.Calendar
import java.util.UUID
import org.p2p.core.utils.MillisSinceEpoch
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.Constants

@Entity(
    tableName = "user_send_links",
    primaryKeys = ["uuid", "owner_address"]
)
data class UserSendLinkEntity(
    @ColumnInfo(name = "uuid")
    val uuid: String,
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
) {
    companion object {
        fun createStubsForDebug(userAddress: Base58String): List<UserSendLinkEntity> = listOf(
            UserSendLinkEntity(
                UUID.randomUUID().toString(),
                link = "link",
                amount = BigDecimal.valueOf(10301231),
                tokenMint = Constants.WRAPPED_SOL_MINT.toBase58Instance(),
                dateCreated = Calendar.getInstance().apply {
                    set(Calendar.YEAR, 1990)
                    set(Calendar.MONTH, 5)
                    set(Calendar.DAY_OF_MONTH, 11)
                }.timeInMillis,
                userAddress
            ),
            UserSendLinkEntity(
                UUID.randomUUID().toString(),
                link = "link",
                amount = BigDecimal.ONE,
                tokenMint = Constants.WRAPPED_SOL_MINT.toBase58Instance(),
                dateCreated = Calendar.getInstance().apply {
                    set(Calendar.YEAR, 1990)
                    set(Calendar.MONTH, 10)
                    set(Calendar.DAY_OF_MONTH, 11)
                }.timeInMillis,
                userAddress
            ),
            UserSendLinkEntity(
                UUID.randomUUID().toString(),
                link = "link",
                amount = BigDecimal.ONE,
                tokenMint = Constants.WRAPPED_SOL_MINT.toBase58Instance(),
                dateCreated = Calendar.getInstance().apply {
                    set(Calendar.YEAR, 1990)
                    set(Calendar.MONTH, 10)
                    set(Calendar.DAY_OF_MONTH, 11)
                }.timeInMillis,
                userAddress
            ),
            UserSendLinkEntity(
                UUID.randomUUID().toString(),
                link = "link",
                amount = BigDecimal.ONE.multiply(BigDecimal.TEN),
                tokenMint = Constants.WRAPPED_SOL_MINT.toBase58Instance(),
                dateCreated = Calendar.getInstance().apply {
                    set(Calendar.YEAR, 1991)
                    set(Calendar.MONTH, 11)
                    set(Calendar.DAY_OF_MONTH, 11)
                }.timeInMillis,
                userAddress
            )
        )
    }
}
