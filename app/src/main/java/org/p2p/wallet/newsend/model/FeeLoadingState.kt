package org.p2p.wallet.newsend.model

/**
 * Sometimes the fee is loading too fast. In this case our UI blinks with too fast loading
 *
 * @constructor [Delayed] will help us to start a delayed loading for such fast requests.
 * If the request is fast, we will delay the loading.
 * The end of the request will cancel the loading if it didn't start yet.
 * Thus we are avoiding the blinking
 * */
sealed interface FeeLoadingState {

    data class Delayed(val isLoading: Boolean) : FeeLoadingState
    data class Instant(val isLoading: Boolean) : FeeLoadingState

    fun isFeeLoading(): Boolean = when (this) {
        is Delayed -> isLoading
        is Instant -> isLoading
    }

    companion object {
        operator fun invoke(isLoading: Boolean, isDelayed: Boolean): FeeLoadingState =
            if (isDelayed) {
                Delayed(isLoading)
            } else {
                Instant(isLoading)
            }
    }
}
