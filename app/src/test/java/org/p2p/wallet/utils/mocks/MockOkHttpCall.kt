package org.p2p.wallet.utils.mocks

import io.mockk.MockKStubScope
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Timeout

/**
 * Using [mockk] on okhttp Call throws an exception: VerifyError, idk what that means, but making mock class helps
 */
class MockOkHttpCall(
    val request: Request,
    val responseBody: ResponseBody,
    private val responseCode: Int = 200,
    private val responseMessage: String = "OK"
) : Call {

    private val response = Response.Builder()
        .request(request)
        .protocol(okhttp3.Protocol.HTTP_1_1)
        .code(responseCode)
        .body(responseBody)
        .message(responseMessage)
        .build()

    override fun clone(): Call {
        return MockOkHttpCall(request, responseBody, responseCode, responseMessage)
    }

    override fun cancel() = Unit
    override fun enqueue(responseCallback: Callback) = responseCallback.onResponse(this, response)
    override fun isCanceled(): Boolean = false
    override fun isExecuted(): Boolean = true
    override fun request(): Request = request
    override fun timeout(): Timeout = Timeout.NONE
    override fun execute(): Response = response
}

infix fun MockKStubScope<Call, Call>.responses(toResponseBody: ResponseBody) = answers {
    MockOkHttpCall(
        request = arg(0),
        responseBody = toResponseBody
    )
}

infix fun MockKStubScope<Call, Call>.answersResponseBody(
    answer: () -> ResponseBody
) = answers {
    MockOkHttpCall(
        request = arg(0),
        responseBody = answer()
    )
}

fun MockKStubScope<Call, Call>.answersResponseBody(
    code: Int = 200,
    message: String = "OK",
    answer: () -> ResponseBody
) = answers {
    MockOkHttpCall(
        request = firstArg(),
        responseBody = answer(),
        responseCode = code,
        responseMessage = message
    )
}
