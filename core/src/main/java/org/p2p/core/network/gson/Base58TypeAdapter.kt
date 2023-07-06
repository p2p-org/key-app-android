package org.p2p.core.network.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import org.p2p.core.crypto.Base58String
import org.p2p.core.utils.decodeFromBase58

object Base58TypeAdapter : JsonSerializer<Base58String>, JsonDeserializer<Base58String> {
    override fun serialize(src: Base58String?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        return src?.base58Value?.let(::JsonPrimitive)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Base58String? {
        return json?.asJsonPrimitive?.asString?.let {
            requireBase58IsValid(it)
            Base58String(it)
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun requireBase58IsValid(string: String) {
        try {
            string.decodeFromBase58()
        } catch (isNotBase58: Throwable) {
            throw IllegalArgumentException("Is not Base58 string: $string", isNotBase58)
        }
    }
}
