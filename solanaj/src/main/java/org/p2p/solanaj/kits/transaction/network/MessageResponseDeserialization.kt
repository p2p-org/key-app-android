package org.p2p.solanaj.kits.transaction.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.p2p.solanaj.kits.transaction.network.transaction.MessageResponse
import java.lang.reflect.Type

object MessageResponseDeserialization : JsonDeserializer<MessageResponse?> {
    val gson = GsonBuilder().setLenient().create()
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): MessageResponse? {
        return try {
            val instructions = json?.asJsonObject?.getAsJsonArray("instructions")
            if (instructions == null) {
                return null
            } else {
                gson.fromJsonReified<MessageResponse?>(json.asJsonObject.toString())
            }
            val result = gson.fromJsonReified<MessageResponse?>(json.asJsonObject.toString())
            result
        } catch (e: Throwable) {
            null
        }
    }
}

inline fun <reified Type> Gson.fromJsonReified(json: String): Type? {
    val result = fromJson<Type>(json, object : TypeToken<Type>() {}.type)
    return result
}
