package org.p2p.ethereumkit.internal.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.p2p.ethereumkit.internal.core.toHexString
import java.math.BigInteger
import java.util.*
import org.p2p.core.wrapper.eth.EthAddress

@Entity
data class InternalTransaction(
    val hash: ByteArray,
    val blockNumber: Long,
    val from: EthAddress,
    val to: EthAddress,
    val value: BigInteger,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
) {

    @delegate:Ignore
    val hashString: String by lazy {
        hash.toHexString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is InternalTransaction)
            return false

        return hash.contentEquals(other.hash) && id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(hash, id)
    }

}
