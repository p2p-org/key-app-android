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
import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.network.SolAddressTypeAdapter

class GsonProvider {

    var gson: Gson? = null

    fun provide(): Gson {
        return gson ?: buildGson()
    }

    private fun buildGson(): Gson {
        return GsonBuilder().apply {
            setLenient()
            registerTypeAdapter(BigInteger::class.java, BigIntegerTypeAdapter())
            registerTypeAdapter(object : TypeToken<BigInteger?>() {}.type, BigIntegerTypeAdapter())
            registerTypeAdapter(Long::class.java, LongTypeAdapter())
            registerTypeAdapter(Int::class.java, IntTypeAdapter())
            registerTypeAdapter(ByteArray::class.java, ByteArrayTypeAdapter())
            registerTypeHierarchyAdapter(DefaultBlockParameter::class.java, DefaultBlockParameterTypeAdapter())
            registerTypeAdapter(EthAddress::class.java, AddressTypeAdapter())
            registerTypeAdapter(SolAddress::class.java, SolAddressTypeAdapter())
            registerTypeAdapter(
                object : TypeToken<Optional<RpcTransaction>>() {}.type,
                OptionalTypeAdapter<RpcTransaction>(RpcTransaction::class.java)
            )
            registerTypeAdapter(
                object : TypeToken<Optional<RpcTransactionReceipt>>() {}.type,
                OptionalTypeAdapter<RpcTransactionReceipt>(RpcTransactionReceipt::class.java)
            )
            registerTypeAdapter(
                object : TypeToken<Optional<RpcBlock>>() {}.type,
                OptionalTypeAdapter<RpcBlock>(RpcBlock::class.java)
            )
        }.create().also {
            gson = it
        }
    }
}


