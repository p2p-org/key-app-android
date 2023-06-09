package org.p2p.core.utils

import com.google.gson.stream.JsonReader

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


