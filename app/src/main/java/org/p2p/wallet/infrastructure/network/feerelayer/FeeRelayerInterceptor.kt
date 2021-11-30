// package org.p2p.wallet.infrastructure.network.interceptor
//
// import org.p2p.wallet.infrastructure.network.ServerException
// import okhttp3.Interceptor
// import okhttp3.Response
//
// TODO: ADD Error handling interceptor for Fee relayer
// https://github.com/p2p-org/FeeRelayerSwift/blob/master/FeeRelayerSwift/Classes/FeeRelayer%2BError.swift
// class FeeRelayerInterceptor : Interceptor {
//
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val request = chain.request()
//        val response = chain.proceed(request)
//        return if (response.isSuccessful) {
//            handleResponse(response)
//        } else {
//            throw extractException(response.body!!.string())
//        }
//    }
//
//    private fun handleResponse() {
//    }
//
//    private fun extractException(): ServerException {
//
//    }
// }