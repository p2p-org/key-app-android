package org.p2p.token.service.model

sealed class MarketPriceResult<T> {
    data class Success<T>(val data: T) : MarketPriceResult<T>()

    sealed class Error(val cause: Throwable) : MarketPriceResult<Throwable>()
}

fun <T> MarketPriceResult<T>.unwrap(): T? {
    return when (this) {
        is MarketPriceResult.Success<T> -> this.data
        is MarketPriceResult.Error -> throw cause
    }
}
