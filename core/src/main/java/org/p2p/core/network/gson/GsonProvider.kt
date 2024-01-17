package org.p2p.core.network.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String
import org.p2p.core.model.DefaultBlockParameter
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress

class GsonProvider {

    var gson: Gson? = null

    private val builder: GsonBuilder = GsonBuilder()

    fun withBuilder(block: GsonBuilder.() -> Unit): GsonProvider {
        block(builder)
        return this
    }

    fun provide(): Gson {
        return gson ?: buildGson()
    }

    private fun buildGson(): Gson {
        return builder.apply {
            setLenient()
            registerTypeAdapter(BigInteger::class.java, BigIntegerTypeAdapter())
            registerTypeAdapter(object : TypeToken<BigInteger?>() {}.type, BigIntegerTypeAdapter())
            registerTypeAdapter(Long::class.java, LongTypeAdapter())
            registerTypeAdapter(Int::class.java, IntTypeAdapter())
            registerTypeAdapter(ByteArray::class.java, ByteArrayTypeAdapter())
            registerTypeAdapter(EthAddress::class.java, AddressTypeAdapter())
            registerTypeAdapter(SolAddress::class.java, SolAddressTypeAdapter())
            registerTypeAdapter(HexString::class.java, HexStringTypeAdapter())
            registerTypeAdapter(Base64String::class.java, Base64StringTypeAdapter())
            registerTypeAdapter(Base58String::class.java, Base58StringTypeAdapter())
            registerTypeHierarchyAdapter(DefaultBlockParameter::class.java, DefaultBlockParameterTypeAdapter())
        }
            .create()
            .also { gson = it }
    }
}
