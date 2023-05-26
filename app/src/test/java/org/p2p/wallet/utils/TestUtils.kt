package org.p2p.wallet.utils

import java.io.InputStream
import java.net.URL

internal fun Any.getTestRawResourceUrl(name: String): URL = javaClass.classLoader!!.getResource(name)
internal fun Any.getTestRawResource(name: String): InputStream = getTestRawResourceUrl(name).openStream()
