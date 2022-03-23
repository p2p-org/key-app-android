package org.p2p.wallet.utils

import android.app.Activity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T : Fragment> T.withArgs(vararg params: Pair<String, Any?>): T {
    arguments = bundleOf(*params)
    return this
}

fun <T : Fragment> T.appendArgs(vararg params: Pair<String, Any?>): T {
    val newArguments = bundleOf(*params)
    arguments = arguments?.apply { putAll(newArguments) } ?: newArguments
    return this
}

inline fun <reified T> Activity.args(key: String? = null, defaultValue: T? = null): ReadWriteProperty<Activity, T> {
    return BundleExtractorDelegate { thisRef, property ->
        val bundleKey = key ?: property.name
        extractFromBundle(thisRef.intent.extras, bundleKey, defaultValue)
    }
}

inline fun <reified T> Fragment.args(key: String? = null, defaultValue: T? = null): ReadWriteProperty<Fragment, T> {
    return BundleExtractorDelegate { thisRef, property ->
        val bundleKey = key ?: property.name
        extractFromBundle(thisRef.arguments, bundleKey, defaultValue)
    }
}

class BundleExtractorDelegate<in R, T>(private val initializer: (R, KProperty<*>) -> T) : ReadWriteProperty<R, T> {

    private object EMPTY

    private var value: Any? = EMPTY

    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        this.value = value
    }

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        if (value == EMPTY) {
            value = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }
}

inline fun <reified T> extractFromBundle(bundle: Bundle?, key: String? = null, defaultValue: T? = null): T {
    val result = bundle?.get(key) ?: defaultValue

    if (result != null && result !is T) {
        throw ClassCastException("Property for $key has different class type")
    }
    return result as T
}

inline fun <reified T> Fragment.getArg(key: String, defaultValue: T? = null) =
    extractFromBundle(arguments, key, defaultValue)
