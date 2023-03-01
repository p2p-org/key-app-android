package org.p2p.ethereumkit.api.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class LastBlockHeight(val height: Long, @PrimaryKey val id: String = "")
