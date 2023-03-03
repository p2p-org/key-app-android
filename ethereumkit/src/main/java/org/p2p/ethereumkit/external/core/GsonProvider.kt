package org.p2p.ethereumkit.external.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.p2p.ethereumkit.internal.api.jsonrpc.models.RpcBlock
import org.p2p.ethereumkit.internal.api.jsonrpc.models.RpcTransaction
import org.p2p.ethereumkit.internal.api.jsonrpc.models.RpcTransactionReceipt
import org.p2p.ethereumkit.internal.models.DefaultBlockParameter
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.ethereumkit.internal.network.AddressTypeAdapter
import org.p2p.ethereumkit.internal.network.BigIntegerTypeAdapter
import org.p2p.ethereumkit.internal.network.ByteArrayTypeAdapter
import org.p2p.ethereumkit.internal.network.DefaultBlockParameterTypeAdapter
import org.p2p.ethereumkit.internal.network.IntTypeAdapter
import org.p2p.ethereumkit.internal.network.LongTypeAdapter
import org.p2p.ethereumkit.internal.network.OptionalTypeAdapter
import java.math.BigInteger
import java.util.Optional

class GsonProvider {

    var gson: Gson? = null

    fun provide(): Gson {
        return gson ?: GsonBuilder().also {
            it.setLenient()
            it.registerTypeAdapter(BigInteger::class.java, BigIntegerTypeAdapter())
            it.registerTypeAdapter(object: TypeToken<BigInteger?>() {}.type, BigIntegerTypeAdapter())
            it.registerTypeAdapter(Long::class.java, LongTypeAdapter())
            it.registerTypeAdapter(Int::class.java, IntTypeAdapter())
            it.registerTypeAdapter(ByteArray::class.java, ByteArrayTypeAdapter())
            it.registerTypeHierarchyAdapter(DefaultBlockParameter::class.java, DefaultBlockParameterTypeAdapter())
            it.registerTypeAdapter(EthAddress::class.java, AddressTypeAdapter())
            it.registerTypeAdapter(
                object : TypeToken<Optional<RpcTransaction>>() {}.type,
                OptionalTypeAdapter<RpcTransaction>(RpcTransaction::class.java)
            )
            it.registerTypeAdapter(
                object : TypeToken<Optional<RpcTransactionReceipt>>() {}.type,
                OptionalTypeAdapter<RpcTransactionReceipt>(RpcTransactionReceipt::class.java)
            )
            it.registerTypeAdapter(
                object : TypeToken<Optional<RpcBlock>>() {}.type,
                OptionalTypeAdapter<RpcBlock>(RpcBlock::class.java)
            )
        }.create().also {
            gson = it
        }
    }
}


