package org.p2p.core.utils

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
