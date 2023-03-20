package org.p2p.ethereumkit.internal.models

import org.p2p.ethereumkit.internal.core.toHexString

class Signature(val v: Int,
                val r: ByteArray,
                val s: ByteArray) {
    override fun toString(): String {
        return "Signature [v: $v; r: ${r.toHexString()}; s: ${s.toHexString()}]"
    }
}
