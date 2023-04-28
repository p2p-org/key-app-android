package org.p2p.wallet.utils

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

inline fun <reified T : Parcelable> Bundle.requireParcelable(key: String): T {
    return if (containsKey(key)) {
        requireNotNull(getParcelableCompat(key))
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

inline fun <reified T : Serializable> Bundle.requireSerializable(key: String): T {
    return if (containsKey(key)) {
        getSerializableCompat(key)!!
    } else {
        throw IllegalArgumentException("Required argument \"$key\" is missing!")
    }
}

inline fun <reified T : Serializable> Bundle.getSerializableOrNull(key: String): T? {
    return if (containsKey(key)) {
        getSerializableCompat(key) as? T
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

fun isTiramisuOrLater() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? {
    return when {
        isTiramisuOrLater() -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key)
    }
}

inline fun <reified T : Parcelable> Bundle.getParcelableArrayListCompat(key: String): ArrayList<T> {
    return when {
        isTiramisuOrLater() -> getParcelableArrayList(key, T::class.java) ?: ArrayList(0)
        else -> @Suppress("DEPRECATION") (getParcelableArrayList(key) ?: ArrayList(0))
    }
}

inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
    return when {
        isTiramisuOrLater() -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key)
    }
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T> {
    return when {
        isTiramisuOrLater() -> getParcelableArrayListExtra(key, T::class.java) ?: ArrayList()
        else -> @Suppress("DEPRECATION") (getParcelableArrayListExtra(key) ?: ArrayList())
    }
}

inline fun <reified T : Serializable> Bundle.getSerializableCompat(key: String): T? {
    return when {
        isTiramisuOrLater() -> getSerializable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializable(key) as? T
    }
}

inline fun <reified T : Serializable> Intent.getSerializableExtraCompat(key: String): T? {
    return when {
        isTiramisuOrLater() -> getSerializableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
    }
}
