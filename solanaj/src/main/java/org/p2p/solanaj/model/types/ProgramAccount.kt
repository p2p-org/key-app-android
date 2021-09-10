package org.p2p.solanaj.model.types

import android.util.Base64
import com.google.gson.annotations.SerializedName
import org.bitcoinj.core.Base58

data class ProgramAccount(
    @SerializedName("account")
    val account: Account,
    @SerializedName("pubkey")
    val pubkey: String
) {

    data class Account(

        @SerializedName("data")
        val data: List<String>,

        @SerializedName("executable")
        val isExecutable: Boolean,

        @SerializedName("lamports")
        val lamports: Double,

        @SerializedName("owner")
        val owner: String,

        @SerializedName("rentEpoch")
        val rentEpoch: Double

    ) {

        fun getDecodedData(): ByteArray =
            if (data[1] == Encoding.BASE64.encoding) {
                Base64.decode(data[0], Base64.DEFAULT)
            } else {
                Base58.decode(data[0])
            }
    }
}