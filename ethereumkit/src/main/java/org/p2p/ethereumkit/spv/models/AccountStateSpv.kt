package org.p2p.ethereumkit.spv.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.p2p.ethereumkit.core.toHexString
import org.p2p.ethereumkit.models.EthAddress
import java.math.BigInteger

@Entity
class AccountStateSpv(
    @PrimaryKey
        val address: EthAddress,
    val nonce: Long,
    val balance: BigInteger,
    val storageHash: ByteArray,
    val codeHash: ByteArray
) {

    override fun toString(): String {
        return "(\n" +
                "  nonce: $nonce\n" +
                "  balance: $balance\n" +
                "  storageHash: ${storageHash.toHexString()}\n" +
                "  codeHash: ${codeHash.toHexString()}\n" +
                ")"
    }
}
