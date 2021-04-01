package com.wowlet.entities.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_item")
class LocalWalletItem(
    @PrimaryKey val id: String = "",
    @ColumnInfo(name = "name") val walletName: String = ""
) {
    override fun toString(): String = "LocalWalletItem(id='$id', walletName='$walletName')"
}