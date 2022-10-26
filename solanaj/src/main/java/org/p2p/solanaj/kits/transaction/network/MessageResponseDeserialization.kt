package org.p2p.solanaj.kits.transaction.network

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
                val result = gson.fromJson<MessageResponse?>(
                    json.asJsonObject.toString(),
                    object :
                        TypeToken<MessageResponse?>() {}.type
                )
                result
            }
        } catch (e: Throwable) {
            null
        }
    }
}
