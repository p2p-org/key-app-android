package org.p2p.wallet.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.InputStream
import java.net.URL

internal fun Any.getTestRawResourceUrl(name: String): URL = javaClass.classLoader!!.getResource(name)
internal fun Any.getTestRawResource(name: String): InputStream = getTestRawResourceUrl(name).openStream()

internal fun createHttpException(code: Int, errorBody: String): HttpException {
    return HttpException(
        Response.error<ResponseBody>(code, errorBody.toResponseBody("application/json".toMediaType()))
    )
}
