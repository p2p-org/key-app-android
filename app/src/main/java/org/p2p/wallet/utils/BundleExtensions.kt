package org.p2p.wallet.utils

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

fun <T : Parcelable> Bundle.requireParcelable(key: String): T {
    return if (containsKey(key)) {
        requireNotNull(getParcelable(key))
    } else {
        throw IllegalArgumentException("Required argument \"$key\" is missing!")
    }
}

fun Bundle.requireString(key: String): String {
    return if (containsKey(key)) {
        requireNotNull(getString(key))
    } else {
        throw IllegalArgumentException("Required argument \"$key\" is missing!")
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> Bundle.requireSerializable(key: String): T {
    return if (containsKey(key)) {
        getSerializable(key) as T
    } else {
        throw IllegalArgumentException("Required argument \"$key\" is missing!")
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> Bundle.getSerializableOrNull(key: String): T? {
    return if (containsKey(key)) {
        getSerializable(key) as T
    } else {
        null
    }
}

fun Bundle.requireInt(key: String): Int {
    return if (containsKey(key)) {
        getInt(key)
    } else {
        throw IllegalArgumentException("Required argument \"$key\" is missing!")
    }
}

fun Bundle.requireLong(key: String): Long {
    return if (containsKey(key)) {
        getLong(key)
    } else {
        throw IllegalArgumentException("Required argument \"$key\" is missing!")
    }
}

fun Bundle.getIntOrNull(key: String): Int? {
    return if (containsKey(key)) {
        getInt(key)
    } else {
        null
    }
}

fun Bundle.getLongOrNull(key: String): Long? {
    return if (containsKey(key)) {
        getLong(key)
    } else {
        null
    }
}
