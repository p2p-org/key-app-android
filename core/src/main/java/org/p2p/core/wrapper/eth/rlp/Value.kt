package org.p2p.core.wrapper.eth.rlp

import java.util.*

class Value(obj: Any?) {

    private var value: Any? = null
    private var rlp: ByteArray? = null
    private var sha3: ByteArray? = null

    private var decoded = false


    init {
        this.decoded = true
        if (obj is Value) {
            this.value = obj.asObj()
        } else {
            this.value = obj
        }
    }

    constructor() : this(null)

    fun init(rlp: ByteArray) {
        this.rlp = rlp
    }

    fun withHash(hash: ByteArray): Value {
        sha3 = hash
        return this
    }

    fun asObj(): Any? {
        decode()
        return value
    }

    fun asList(): List<Any> {
        decode()
        val valueArray = value as Array<Any>
        return listOf(*valueArray)
    }

    fun decode() {
        if (!this.decoded) {
            this.value = rlp?.let { RLP.decode(it, 0).decoded }
            this.decoded = true
        }
    }

    fun isList(): Boolean {
        decode()
        return value?.let { it.javaClass.isArray && it.javaClass.componentType?.isPrimitive == false }
                ?: false
    }
}
