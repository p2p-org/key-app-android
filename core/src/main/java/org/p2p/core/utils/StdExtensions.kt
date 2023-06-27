package org.p2p.core.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.koin.ext.getFullName
import timber.log.Timber
import java.lang.reflect.Type

typealias MillisSinceEpoch = Long // to replace ambiguous Long in some places connected to dates

fun <T> List<T>.merge(second: List<T>): List<T> = this + second

fun <T> MutableList<T>.addIf(predicate: Boolean, value: T) {
    if (predicate) add(value)
}

fun <T> MutableList<T>.addIf(predicate: () -> Boolean, value: T) {
    addIf(predicate.invoke(), value)
}

fun <T> MutableList<T>.addIf(predicate: Boolean, vararg values: T) {
    if (predicate) addAll(values)
}

fun <T : Any> JsonReader.nextObject(objectScope: (JsonReader) -> T): T {
    beginObject()
    val result = objectScope.invoke(this)
    endObject()
    return result
}

fun <T : Any> JsonReader.nextArray(arrayScope: (JsonReader) -> T): T {
    beginArray()
    val result = arrayScope.invoke(this)
    endArray()
    return result
}


fun Result<*>.invokeAndForget() {
    getOrNull()
}


fun Gson.toJsonObject(obj: Any): JsonObject {
    val objectAsJsonStr = toJson(obj).takeIf { obj !is String }
    return fromJsonReified<JsonObject>(objectAsJsonStr ?: obj.toString())
        ?: error("Failed to convert object $objectAsJsonStr ($obj) to JsonObject")
}

inline fun <reified Type> Gson.fromJsonReified(json: String): Type? {
    val result = fromJson<Type>(json, object : TypeToken<Type>() {}.type)
    if (result == null) {
        Timber.e(IllegalArgumentException("Couldn't parse ${Type::class.getFullName()} from json: ${json.take(30)}"))
    }
    return result
}
inline fun <reified T>Gson.fromJsonReified(json: JsonElement, typeToken: Type):  T {
    val result = fromJson<T>(json,typeToken)
    return result
}

fun Request.bodyAsString(): String = kotlin.runCatching {
    val requestCopy: Request = this.newBuilder().build()
    val buffer = Buffer()
    requestCopy.body?.writeTo(buffer)
    buffer.readUtf8()
}
    .getOrDefault("")

fun Response.bodyAsString(): String = peekBody(Long.MAX_VALUE).string()

