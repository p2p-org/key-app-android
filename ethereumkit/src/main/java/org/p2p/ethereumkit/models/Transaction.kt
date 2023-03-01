package org.p2p.ethereumkit.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.p2p.ethereumkit.core.toHexString
import java.math.BigInteger

@Entity
class Transaction(
    @PrimaryKey
    val hash: ByteArray,
    val timestamp: Long,
    var isFailed: Boolean,

    val blockNumber: Long? = null,
    val transactionIndex: Int? = null,
    val from: EthAddress? = null,
    val to: EthAddress? = null,
    val value: BigInteger? = null,
    val input: ByteArray? = null,
    val nonce: Long? = null,
    val gasPrice: Long? = null,
    val maxFeePerGas: Long? = null,
    val maxPriorityFeePerGas: Long? = null,
    val gasLimit: Long? = null,
    val gasUsed: Long? = null,

    var replacedWith: ByteArray? = null
) {

    @delegate:Ignore
    val hashString: String by lazy {
        hash.toHexString()
    }

}
