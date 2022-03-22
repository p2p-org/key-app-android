package org.p2p.wallet.infrastructure.network.data

import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.jvm.Throws

/**
 * Currently backend sends us data in format:
 * {
 *      jsonrpc: "2.0",
 *      result: {
 *          data: "some data"
 *      }
 *      id: "someid"
 * }
 *
 * This converter allows all apis define only [result] response model: { data: "some data" }
 *
 * In case of errors, we will receive IOException
 * */

class ResponseConverterFactory(private val gson: Gson) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val wrappedType = object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type> = arrayOf(type)
            override fun getOwnerType(): Type? = null
            override fun getRawType(): Type = CommonResponse::class.java
        }
        val factory = GsonConverterFactory.create(gson)
        val converter = factory.responseBodyConverter(wrappedType, annotations, retrofit)
        @Suppress("UNCHECKED_CAST")
        return ResponseBodyConverter(converter as Converter<ResponseBody, CommonResponse<Any>>)
    }
}

class ResponseBodyConverter<T>(
    private val converter: Converter<ResponseBody, CommonResponse<T>>
) : Converter<ResponseBody, T> {

    @Throws(IOException::class)
    override fun convert(responseBody: ResponseBody): T? {
        if (responseBody.contentLength() == 0L) return null
        val response = converter.convert(responseBody)
        return response?.result
    }
}
