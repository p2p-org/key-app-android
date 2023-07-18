package org.p2p.wallet.utils

import io.mockk.MockKAnnotations
import io.mockk.MockKException
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.spyk
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Does not support mockk in parameterized JUnit 5 tests.
 */
class SpyOnInjectMockKsExtension : TestInstancePostProcessor {
    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext?) {
        MockKAnnotations.init(testInstance, relaxUnitFun = true)

        assignSpyKOnInjectMockKs(testInstance)
    }

    @Suppress("UNCHECKED_CAST")
    private fun assignSpyKOnInjectMockKs(target: Any) {
        val property: KProperty1<Any, Any> = target::class.memberProperties
            .map { it as KProperty1<Any, Any> }
            .find { it.findAnnotation<InjectMockKs>() != null } ?: return

        if (property !is KMutableProperty1<Any, Any>) {
            throw MockKException(
                "Annotation @InjectMockKs present on ${property.name} read-only property," +
                    " make it read-write please('lateinit var' or 'var')"
            )
        }

        property.set(target, spyk(property.get(target)))
    }
}
