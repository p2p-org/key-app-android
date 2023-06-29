package org.p2p.core.network.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type
import java.math.BigInteger
import java.util.Optional
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.core.wrapper.eth.hexStringToBigIntegerOrNull
import org.p2p.core.wrapper.eth.hexStringToByteArrayOrNull
import org.p2p.core.wrapper.eth.hexStringToIntOrNull
import org.p2p.core.wrapper.eth.hexStringToLongOrNull
import org.p2p.core.wrapper.eth.toHexString

internal class BigIntegerTypeAdapter(private val isHex: Boolean = true) : TypeAdapter<BigInteger?>() {
    override fun write(writer: JsonWriter, value: BigInteger?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val stringValue = if (isHex) value.toHexString() else value.toString()
            writer.value(stringValue)
        }
    }

    override fun read(reader: JsonReader): BigInteger? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val stringValue = reader.nextString()
        return if (isHex) stringValue.hexStringToBigIntegerOrNull() else BigInteger(stringValue)
    }
}

internal class LongTypeAdapter(private val isHex: Boolean = false) : TypeAdapter<Long?>() {
    override fun write(writer: JsonWriter, value: Long?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val stringValue = if (isHex) value.toHexString() else value.toString()
            writer.value(stringValue)
        }
    }

    override fun read(reader: JsonReader): Long? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val stringValue = reader.nextString()
        return if (isHex) stringValue.hexStringToLongOrNull() else stringValue.toLongOrNull()
    }
}

internal class IntTypeAdapter(private val isHex: Boolean = false) : TypeAdapter<Int?>() {
    override fun write(writer: JsonWriter, value: Int?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val stringValue = if (isHex) value.toHexString() else value.toString()
            writer.value(stringValue)
        }
    }

    override fun read(reader: JsonReader): Int? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val stringValue = reader.nextString()
        return if (isHex) stringValue.hexStringToIntOrNull() else stringValue.toIntOrNull()
    }
}

internal class ByteArrayTypeAdapter : TypeAdapter<ByteArray?>() {
    override fun write(writer: JsonWriter, value: ByteArray?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(value.toHexString())
        }
    }

    override fun read(reader: JsonReader): ByteArray? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        return reader.nextString().hexStringToByteArrayOrNull()
    }
}

internal class AddressTypeAdapter : TypeAdapter<EthAddress?>() {
    override fun write(writer: JsonWriter, value: EthAddress?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(value.hex)
        }
    }

    override fun read(reader: JsonReader): EthAddress? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        return try {
            EthAddress(reader.nextString())
        } catch (error: Throwable) {
            null
        }
    }
}

internal class SolAddressTypeAdapter : TypeAdapter<SolAddress?>() {

    override fun write(writter: JsonWriter, value: SolAddress?) {
        if (value == null) {
            writter.nullValue()
        } else {
            writter.value(value.raw)
        }
    }

    override fun read(reader: JsonReader): SolAddress? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        return try {
            SolAddress(reader.nextString())
        } catch (error: Throwable) {
            null
        }
    }
}

internal class HexStringTypeAdapter : TypeAdapter<HexString?>() {

    override fun write(writter: JsonWriter, value: HexString?) {
        if (value == null) {
            writter.nullValue()
        } else {
            writter.value(value.rawValue)
        }
    }

    override fun read(reader: JsonReader): HexString? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        return try {
            HexString(reader.nextString())
        } catch (error: Throwable) {
            null
        }
    }
}


internal class OptionalTypeAdapter<T>(
    private val type: Type,
) : TypeAdapter<Optional<T>>() {

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(EthAddress::class.java, AddressTypeAdapter())
        .registerTypeAdapter(ByteArray::class.java, ByteArrayTypeAdapter())
        .registerTypeAdapter(BigInteger::class.java, BigIntegerTypeAdapter())
        .registerTypeAdapter(Long::class.java, LongTypeAdapter())
        .registerTypeAdapter(object : TypeToken<Long?>() {}.type, LongTypeAdapter())
        .registerTypeAdapter(Int::class.java, IntTypeAdapter())
        .registerTypeAdapter(object : TypeToken<Int?>() {}.type, IntTypeAdapter())
        .create()

    override fun write(writer: JsonWriter, value: Optional<T>) {
        if (value.isPresent) {
            gson.toJson(gson.toJsonTree(value.get(), type), writer)
        } else {
            writer.nullValue()
        }
    }

    override fun read(reader: JsonReader): Optional<T> {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return Optional.empty()
        }
        return Optional.of(gson.fromJson(reader, type))
    }
}
