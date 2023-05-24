package org.p2p.ethereumkit.internal.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.p2p.ethereumkit.internal.core.toHexString

@Parcelize
class Signature(
    @SerializedName("v")
    val v: Int,
    @SerializedName("r")
    val r: ByteArray,
    @SerializedName("s")
    val s: ByteArray
) : Parcelable {
    override fun toString(): String {
        return "Signature [v: $v; r: ${r.toHexString()}; s: ${s.toHexString()}]"
    }
}
