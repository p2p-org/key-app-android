package org.p2p.core.utils

/**
 * Use this annotation to exclude classes from coverage report.
 * It doesn't work for code that is already tested.
 * Also it doesn't work yet for inner/companion classes, waiting for fixing
 * @see [https://github.com/Kotlin/kotlinx-kover/issues/331]
 */
@Retention(AnnotationRetention.BINARY)
annotation class NoCoverage
