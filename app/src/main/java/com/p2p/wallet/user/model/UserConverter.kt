package com.p2p.wallet.user.model

import com.p2p.wallet.common.crypto.Base58Utils
import com.p2p.wallet.common.crypto.Base64Utils
import org.bitcoinj.core.Utils
import org.p2p.solanaj.rpc.types.ProgramAccount

object UserConverter {

//    fun fromNetwork(response: ProgramAccount): TokenAccount {
//        val data = Base58Utils.decode(response.account.data)
//        val amount = Utils.readInt64(data, 32 + 32)
//        val mintArray = data.sliceArray(IntRange(0, 32))
//        return TokenAccount(
//            depositAddress = response.pubkey,
//            amount = amount,
//            mintAddress = Base58Utils.encode(mintArray)
//        )
//    }

    fun fromNetwork(response: ProgramAccount): TokenAccount {
        val data = Base58Utils.decode(response.account.data)

        val mintData = ByteArray(32)
        System.arraycopy(data, 0, mintData, 0, 32)

        val mint = Base58Utils.encode(mintData)
        val total = Utils.readInt64(data, 32 + 32)

        return TokenAccount(response.pubkey, total, mint)
    }

    fun fromNetwork(data: List<String>): Int {
        val stringToDecode = data.firstOrNull() ?: return 0
        val decodedArray = Base64Utils.decode(stringToDecode)
        return decodedArray[44].toInt()
    }
}