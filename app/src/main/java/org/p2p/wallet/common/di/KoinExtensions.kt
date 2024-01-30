package org.p2p.wallet.common.di

import org.koin.core.annotation.KoinInternalApi
import org.koin.core.definition.BeanDefinition
import org.koin.core.instance.InstanceFactory
import org.koin.core.module.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.module._factoryInstanceFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.setupInstance
import org.koin.core.scope.Scope

// why kotlin still hasn't introduced compile-time vararg templates?

@OptIn(KoinInternalApi::class)
inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    > Module.factoryOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> R,
    options: BeanDefinition<R>.() -> Unit
): KoinDefinition<R> = setupInstance(_factoryInstanceFactory(definition = { new(constructor) }), options)

/**
 * @see factoryOf
 */
inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    > Module.factoryOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> R,
): Pair<Module, InstanceFactory<R>> = factory { new(constructor) }

inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11
    > Scope.new(
    constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> R
): R = constructor(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())

@OptIn(KoinInternalApi::class)
inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    > Module.factoryOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) -> R,
    options: BeanDefinition<R>.() -> Unit
): KoinDefinition<R> = setupInstance(_factoryInstanceFactory(definition = { new(constructor) }), options)

/**
 * @see factoryOf
 */
inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    > Module.factoryOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) -> R,
): Pair<Module, InstanceFactory<R>> = factory { new(constructor) }

inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12
    > Scope.new(
    constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) -> R
): R = constructor(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())

@OptIn(KoinInternalApi::class)
inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    reified T13,
    > Module.factoryOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) -> R,
    options: BeanDefinition<R>.() -> Unit
): KoinDefinition<R> = setupInstance(_factoryInstanceFactory(definition = { new(constructor) }), options)

/**
 * @see factoryOf
 */
inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    reified T13,
    > Module.factoryOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) -> R,
): Pair<Module, InstanceFactory<R>> = factory { new(constructor) }

inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    reified T13,
    > Scope.new(
    constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) -> R
): R = constructor(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())

@OptIn(KoinInternalApi::class)
inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    reified T13,
    reified T14,
    > Module.factoryOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) -> R,
    options: BeanDefinition<R>.() -> Unit
): KoinDefinition<R> = setupInstance(_factoryInstanceFactory(definition = { new(constructor) }), options)

/**
 * @see factoryOf
 */
inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    reified T13,
    reified T14,
    > Module.factoryOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) -> R,
): Pair<Module, InstanceFactory<R>> = factory { new(constructor) }

inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    reified T13,
    reified T14,
    > Scope.new(
    constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) -> R
): R = constructor(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())

@OptIn(KoinInternalApi::class)
inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    reified T13,
    reified T14,
    reified T15,
    > Module.factoryOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) -> R,
    options: BeanDefinition<R>.() -> Unit
): KoinDefinition<R> = setupInstance(_factoryInstanceFactory(definition = { new(constructor) }), options)

/**
 * @see factoryOf
 */
inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    reified T13,
    reified T14,
    reified T15,
    > Module.factoryOf(
    crossinline constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) -> R,
): Pair<Module, InstanceFactory<R>> = factory { new(constructor) }

inline fun <
    reified R,
    reified T1,
    reified T2,
    reified T3,
    reified T4,
    reified T5,
    reified T6,
    reified T7,
    reified T8,
    reified T9,
    reified T10,
    reified T11,
    reified T12,
    reified T13,
    reified T14,
    reified T15,
    > Scope.new(
    constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) -> R
): R =
    constructor(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
