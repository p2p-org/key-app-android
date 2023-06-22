package org.p2p.wallet.utils

import io.mockk.every
import io.mockk.mockk
import assertk.Assert
import assertk.assertThat
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.InputStream
import java.net.URL
import org.p2p.wallet.common.feature_toggles.toggles.inapp.InAppFeatureFlag
import kotlin.random.Random

internal fun Any.getTestRawResourceUrl(name: String): URL = javaClass.classLoader!!.getResource(name)
internal fun Any.getTestRawResource(name: String): InputStream = getTestRawResourceUrl(name).openStream()

internal fun createHttpException(code: Int, errorBody: String): HttpException {
    return HttpException(
        Response.error<ResponseBody>(code, errorBody.toResponseBody("application/json".toMediaType()))
    )
}

inline fun <reified T : InAppFeatureFlag> mockInAppFeatureFlag(returns: Boolean = false): T {
    return mockk(relaxed = true) {
        every { featureValue } returns false
    }
}

fun generateRandomBytes(length: Int = 32): ByteArray = Random.Default.nextBytes(length)

fun <T> T.assertThat(): Assert<T> = assertThat(this)

fun <T> T.stub(body: T.() -> Unit) = body.invoke(this)
