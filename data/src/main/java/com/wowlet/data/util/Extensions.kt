package com.wowlet.data.util

import com.wowlet.entities.CallException
import com.wowlet.entities.Result
import com.wowlet.entities.responce.ResponceDataBonfida
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

fun <R> analyzeResponseObject(
    response: Response<ResponceDataBonfida<R>>
): Result<R> {
    when (response.code()) {
        200 -> {
            return response.body()?.let {
                if (it.success)
                    Result.Success(it.data)
                else {
                    Result.Error(CallException<Nothing>(response.code()))
                }
            } ?: Result.Error(CallException<Nothing>(response.code()))
        }
        else -> {
            return Result.Error(CallException<Nothing>(response.code()))
        }
    }
}
fun <R> analyzeResponseList(
    response: Response<ResponceDataBonfida<List<R>>>
): Result<List<R>> {
    when (response.code()) {
        200 -> {
            return response.body()?.let {
                if (it.success)
                    Result.Success(it.data)
                else {
                    Result.Error(CallException<Nothing>(response.code()))
                }
            } ?: Result.Error(CallException<Nothing>(response.code()))
        }
        else -> {
            return Result.Error(CallException<Nothing>(response.code()))
        }
    }
}

