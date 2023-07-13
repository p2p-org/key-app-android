package org.p2p.wallet.utils

import assertk.Assert
import com.google.gson.Gson
import io.mockk.MockKVerificationScope
import io.mockk.verify
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.InputStream
import java.net.URL
import kotlin.random.Random
import org.p2p.core.utils.fromJsonReified

fun Any.getTestRawResourceUrl(name: String): URL = javaClass.classLoader!!.getResource(name)
fun Any.getTestRawResource(name: String): InputStream = getTestRawResourceUrl(name).openStream()

fun createHttpException(code: Int, errorBody: String): HttpException {
    return HttpException(
        Response.error<ResponseBody>(code, errorBody.toResponseBody("application/json".toMediaType()))
    )
}

inline fun <reified T> String.fromJson(gson: Gson): T {
    return gson.fromJsonReified<T>(this) ?: error("Can't parse json")
}

fun generateRandomBytes(length: Int = 32): ByteArray = Random.Default.nextBytes(length)

fun <T> T.assertThat(): Assert<T> = assertk.assertThat(this)

fun <T> Collection<T>.assertThat(): Assert<Collection<T>> = assertk.assertThat(this)

fun <T> T.stub(body: T.() -> Unit) = body.invoke(this)

fun verifyNone(block: MockKVerificationScope.() -> Unit) {
    verify(exactly = 0, verifyBlock = { block() })
}

fun verifyOnce(block: MockKVerificationScope.() -> Unit) {
    verify(exactly = 1, verifyBlock = { block() })
}
