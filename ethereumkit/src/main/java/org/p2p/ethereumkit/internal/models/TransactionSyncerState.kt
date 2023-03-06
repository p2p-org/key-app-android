package org.p2p.ethereumkit.internal.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TransactionSyncerState(
    @PrimaryKey
    val syncerId: String,
    val lastBlockNumber: Long
)
