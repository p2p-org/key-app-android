package org.p2p.ethereumkit.internal.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.ethereumkit.internal.core.toHexString

@Parcelize
class Signature(val v: Int,
                val r: ByteArray,
                val s: ByteArray): Parcelable {
    override fun toString(): String {
        return "Signature [v: $v; r: ${r.toHexString()}; s: ${s.toHexString()}]"
    }
}
