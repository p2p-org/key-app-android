package org.p2p.wallet.home.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.solanaj.utils.crypto.decodeFromBase64
import java.lang.reflect.Type

object Base64TypeAdapter : JsonSerializer<Base64String>, JsonDeserializer<Base64String> {
    override fun serialize(src: Base64String?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        return src?.base64Value?.let(::JsonPrimitive)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Base64String? {
        return json?.asJsonPrimitive?.asString?.let {
            requireBase64IsValid(it)
            Base64String(it)
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun requireBase64IsValid(string: String) {
        try {
            string.decodeFromBase64()
        } catch (isNotBase64: Throwable) {
            throw IllegalArgumentException("Is not Base64 string: $string", isNotBase64)
        }
    }
}
