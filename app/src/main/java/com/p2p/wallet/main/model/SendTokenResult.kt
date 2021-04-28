package com.p2p.wallet.main.model

import androidx.annotation.StringRes

sealed class SendTokenResult {
    object Success : SendTokenResult()
    data class Error(@StringRes val messageRes: Int) : SendTokenResult()
}