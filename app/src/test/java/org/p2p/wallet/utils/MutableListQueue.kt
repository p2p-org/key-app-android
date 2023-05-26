package org.p2p.wallet.utils

/**
 * MutableListQueue as type alias of Mutable List
 */
typealias MutableListQueue<T> = MutableList<T>

/**
 * Pushes item to [MutableListQueue]
 * @param item Item to be pushed
 */
fun <T> MutableListQueue<T>.push(item: T) = add(item)

/**
 * Pops (removes and return) first item from [MutableListQueue]
 * @return item Last item if [MutableListQueue] is not empty, null otherwise
 */
fun <T> MutableListQueue<T>.pop(): T? = if (isNotEmpty()) removeAt(0) else null

/**
 * Peeks (return) last item from [MutableListQueue]
 * @return item Last item if [MutableListQueue] is not empty, null otherwise
 */
fun <T> MutableListQueue<T>.front(): T? = if (isNotEmpty()) this[0] else null

/**
 * Peeks (return) last item from [MutableListQueue]
 * @return item Last item if [MutableListQueue] is not empty, null otherwise
 */
fun <T> MutableListQueue<T>.back(): T? = if (isNotEmpty()) this[lastIndex] else null

inline fun <reified T> mutableListQueueOf(vararg elements: T): MutableListQueue<T> = mutableListOf(*elements)
