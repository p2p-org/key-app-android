package org.p2p.ethereumkit.internal.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.p2p.ethereumkit.internal.core.*
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.ethereumkit.internal.models.DefaultBlockParameter
import java.lang.reflect.Type
import java.math.BigInteger
import java.util.*

class BigIntegerTypeAdapter(private val isHex: Boolean = true) : TypeAdapter<BigInteger?>() {
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

class LongTypeAdapter(private val isHex: Boolean = true) : TypeAdapter<Long?>() {
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

class IntTypeAdapter(private val isHex: Boolean = true) : TypeAdapter<Int?>() {
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

class ByteArrayTypeAdapter : TypeAdapter<ByteArray?>() {
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

class AddressTypeAdapter : TypeAdapter<EthAddress?>() {
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

class DefaultBlockParameterTypeAdapter : TypeAdapter<DefaultBlockParameter?>() {
    override fun write(writer: JsonWriter, value: DefaultBlockParameter?) {
        value?.let {
            writer.value(value.raw)
        } ?: writer.nullValue()
    }

    override fun read(reader: JsonReader): DefaultBlockParameter? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        return DefaultBlockParameter.fromRaw(reader.nextString())
    }
}

class OptionalTypeAdapter<T>(
        private val type: Type
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
