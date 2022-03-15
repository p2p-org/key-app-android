package org.p2p.wallet.utils

import com.google.gson.Gson
import org.p2p.wallet.R

fun <R> unsafeLazy(initializer: () -> R): Lazy<R> = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun <reified R> List<*>.findInstance(): R? {
    return find { it is R }
        ?.let { it as R }
}

inline fun <reified Type> Gson.fromJsonReified(json: String): Type = fromJson(json, Type::class.java)