package com.wowlet.data.util

import com.wowlet.entities.CallException
import com.wowlet.entities.Result
import retrofit2.Response
import java.lang.Exception


suspend fun <R> makeApiCall(
    call: suspend () -> Result<R>,
    errorMessage: Int = 4567
) = try {
    call()
} catch (e: Exception) {
    Result.Error(CallException<Nothing>(errorMessage))
}

fun <R> analyzeResponse(
    response: Response<R>
): Result<R> {
    when (response.code()) {
        200 -> {
            return response.body()?.let {
                Result.Success(it)
            } ?: Result.Error(CallException<Nothing>(response.code()))
        }
        else -> {
            return Result.Error(CallException<Nothing>(response.code()))
        }
    }
}

