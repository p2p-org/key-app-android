package org.p2p.wallet.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.ext.getFullName
import org.p2p.wallet.R
import timber.log.Timber

fun <R> unsafeLazy(initializer: () -> R): Lazy<R> = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun <reified R> List<*>.findInstance(): R? {
    return find { it is R }
        ?.let { it as R }
}

inline fun <E> List<E>.ifSizeNot(expectedSize: Int, defaultValue: (originalList: List<E>) -> List<E>): List<E> {
    return if (this.size != expectedSize) defaultValue.invoke(this) else this
}

inline fun <E> List<E>.ifNotEmpty(action: (originalList: List<E>) -> Unit): List<E> {
    if (isNotEmpty()) action.invoke(this)
    return this
}

inline fun <reified Type> Gson.fromJsonReified(json: String): Type? {
    val result = fromJson<Type>(json, object : TypeToken<Type>() {}.type)
    if (result == null) {
        Timber.e(IllegalArgumentException("Couldn't parse ${Type::class.getFullName()} from json: ${json.take(30)}"))
    }
    return result
}

fun Result<*>.invokeAndForget() {
    getOrNull()
}

fun String.removeWhiteSpaces(): String = replace(" ", emptyString())
